package tables;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Implements a hash-based table
 * using an array data structure.
 */
public class HashArrayTable extends Table {

	private Object[] array; 
	private int arraySize;
	private int contamination; 
	private static final List<Object> TOMBSTONE = List.of();




	/**
	 * Creates a table and initializes
	 * the data structure.
	 *
	 * @param tableName the table name
	 * @param columnNames the column names
	 * @param columnTypes the column types
	 * @param primaryIndex the primary index
	 */
	public HashArrayTable(String tableName, List<String> columnNames, List<String> columnTypes, Integer primaryIndex) {
		setTableName(tableName);
		setColumnNames(columnNames);
		setColumnTypes(columnTypes);
		setPrimaryIndex(primaryIndex);

		
		clear();


	}

	@Override
	public void clear() {
		array = new Object [19];
		contamination =0;
		arraySize =0;
		}
	

	@SuppressWarnings("unchecked")
	@Override
	public boolean put(List<Object> row) {
		
		if(loadFactor()>=0.75){
			rehash();	
			}
		
		int hash = hashFunction(row.get(primaryIndex));
        int index = hash % array.length;
        int firstTombstone = -1;

        for (int i = 0; i < array.length; i++) {

            int asqp = (int) (Math.pow(i, 2));

            if (i % 2 == 0) {
                asqp = -asqp;
            }

            int newIndex = Math.floorMod(index + asqp, array.length);
            

		if(array[newIndex]==null) {
			if(firstTombstone!=-1) {
				array[firstTombstone]=row;
				arraySize++;
				contamination --;
				return false;
			} else {
				array[newIndex]=row;
				arraySize++;
				return false;
			}
		} else {
			if(array[newIndex].equals(TOMBSTONE)) {
				if(firstTombstone==-1) {
				firstTombstone=newIndex;
				}
			} else {
				Object curRow = (List<Object>) array[newIndex];
							if(((List<Object>) curRow).get(getPrimaryIndex()).equals(row.get(getPrimaryIndex()))){
								array[newIndex]=row;
								return true;
							}
			}
		}
		
        }
				

        
        
        return true;

	
	}
	


	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object key) {
		
		
		int hash = hashFunction(key);
        int index = hash % array.length;
        int firstTombstone = -1;

        for (int i = 0; i < array.length; i++) {

            int asqp = (int) (Math.pow(i, 2));

            if (i % 2 == 0) {
                asqp = -asqp;
            }

            int newIndex = Math.floorMod(index + asqp, array.length);

		if (key == null) throw new NullPointerException();

		if(array[newIndex]==null) {
			if(firstTombstone!=-1) {
				array[firstTombstone]=TOMBSTONE;
				return false;
			} else {
				return false;
			}
		} else {
			if(array[newIndex].equals(TOMBSTONE)) {
				if(firstTombstone==-1) {
				firstTombstone=newIndex;
				}
			} else {
				
				Object newKey = ((List<Object>) array[newIndex]).get(getPrimaryIndex());
							if(newKey.equals((key))){
								array[newIndex]=TOMBSTONE;
								arraySize --;
								contamination ++;
								return true;
							}
			}
		} 
        }
				

        
        
        return true;
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<Object> get(Object key) {
		

		int hash = hashFunction(key);
        int index = hash % array.length;
        int firstTombstone = -1;

        for (int i = 0; i < array.length; i++) {

            int asqp = (int) (Math.pow(i, 2));

            if (i % 2 == 0) {
                asqp = -asqp;
            }

            int newIndex = Math.floorMod(index + asqp, array.length);
            

		if (key == null) throw new NullPointerException();

		if(array[newIndex]==null) {
				return null;
			} 
		  if(array[newIndex]!=TOMBSTONE){
			Object newKey = ((List<Object>) array[newIndex]).get(getPrimaryIndex());
							if(newKey.equals(key)){
								return (List<Object>) array[newIndex];
							}
			}
		}
		return null; 
        }
		

	@Override
	public int size() {
		return arraySize;
	}

	@Override
	public int capacity() {
		return array.length;
	}

	@Override
	public Iterator<List<Object>> iterator() {
		return new Iterator<>() {
			int index = 0;



			@Override
			public boolean hasNext() {
				for (int i = index; i < array.length; i++) {
                    if (array[i] != null && array[i] != TOMBSTONE) {
                        return true;
                    }
                }
                return false;
			}


			@SuppressWarnings("unchecked")
			@Override
			public List<Object> next() {	
				for (int i = index; i < array.length; i++) {
                    if (array[i] != null && (!((List<Object>) array[i]).isEmpty())) {
                        index = i + 1;
                        return (List<Object>) array[i];
                    }
                }
                return null;

			}
		};


	}


	public int hashFunction(Object key) {
        if (key instanceof String s) {
            int hash = 1876;
            for (int index = 0; index < s.length(); index++) {
                hash = hash + s.charAt(index);
                hash = hash * 7;
            }
            return Math.floorMod(hash, capacity()); 

        } else {
            return (key.hashCode() % capacity());
        }

    }
	
	
	public boolean prime (int i) {
		for(int k =2; k<i; k++) {
			if(i%k==0) {
				return false;
			}
		}
		return true;
	}
			


	



	@SuppressWarnings("unchecked")
	public void rehash () {
		int newCap = array.length *2;
		while((prime(newCap)&& newCap%4 == 3)) {
			newCap++;
		}
		Object oldArr[] = array;
		array = new Object[newCap];
		arraySize=0;
		
		for(int i = 0; i<oldArr.length; i++) {
			if(oldArr[i]!=TOMBSTONE && oldArr[i]!=null) {
				put((List<Object>)oldArr[i]);
			}
		}

//	for(int k=0; k<array.length; k++) {
//		int num= (int) ((int) Math.pow(-1, k) * (Math.pow(k, 2)));
//		int place = (int) Math.pow(num, 2)+hashedKey;
//		int asqp= Math.floorMod(hashedKey+place, capacity());
//		if(place<capacity()) {

}
}















