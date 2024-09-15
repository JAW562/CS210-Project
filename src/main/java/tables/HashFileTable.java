package tables;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class HashFileTable extends Table {
	private FileChannel file;
	private MappedByteBuffer buff;
	private int range = 9092;
	private int size = 0;
	private int recordSize = 0;
	private int headerSize = 0;
	private int columnNum = 0;

	public HashFileTable(String tableName, List<String> columnNames, List<String> columnTypes, Integer primaryIndex) {
		setTableName(tableName);
		setColumnNames(columnNames);
		setColumnTypes(columnTypes);
		setPrimaryIndex(primaryIndex);
		columnNum = columnNames.size();
		writeHeader();
		writeSchema();
	}

	public HashFileTable(String tableName) {
		Set<StandardOpenOption> op = new HashSet<StandardOpenOption>();
		op.add(StandardOpenOption.WRITE);
		op.add(StandardOpenOption.READ);
		
		
		try {
			file = FileChannel.open(Paths.get("data", tableName), op);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			buff = file.map(MapMode.READ_WRITE, 0, 4);
			headerSize = buff.getInt();
			buff = file.map(MapMode.READ_WRITE, 0, headerSize);
			headerSize = buff.getInt();
			recordSize = buff.getInt();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		range = buff.getInt();
		size = buff.getInt();
		columnNum = buff.getInt();
		this.tableName = readString1();
		columnNames = new LinkedList<String>();
		columnTypes = new LinkedList<String>();

		
		for (var i = 0; i < columnNum; i++)
			columnNames.add(readString1());
		
		for (var i = 0; i < columnNum; i++)
			columnTypes.add(readString1());
		
		primaryIndex = buff.getInt();

		try {
			buff = file.map(MapMode.READ_WRITE, headerSize, recordSize * range);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public int capacity() {
		return range;
	}
	
	@Override
	public int size() {
		return size;
	}

	@Override
	public void clear() {
		size = 0;
		writeSize();
	}

	@Override
	public boolean put(List<Object> row) {
		var key = row.get(primaryIndex);
		int hashKey = toHash(key);
		buff.position(hashKey * recordSize);
		int mask = buff.getInt();
		
		boolean inTable = false;
		boolean currentPos = false;
		currentPos = (mask & 1) == 1;
		int index = hashKey;
		int i = 0;
		
		while (currentPos && i < range) {
			var t = getRecord(index);
			if (key.equals(t.get(primaryIndex))) {
				inTable = true;
				break;
			}
			i++;
			index = (hashKey + i + i * i) % range;
			buff.position((index) * recordSize);
			mask = buff.getInt();
			currentPos = (mask & 1) == 1;
		}
		writeRecord(index, row);

		if (!inTable) {
			size++;
			writeSize();
		}
		return inTable;
	}
	
	@Override
	public List<Object> get(Object key) {
		
		var hashKey = toHash(key);
		buff.position(hashKey * recordSize);
		int mask = buff.getInt();
		boolean currentPos = false;
		boolean inTable = false;
		currentPos = (mask & 1) == 1;
		int i = 0;
		int index = hashKey;
		List<Object> row = null;
		
		while (currentPos && i < range) {
			var t = getRecord(index);
			if (key.equals(t.get(primaryIndex))) {
				inTable = true;
				break;
			}
			i++;
			index = (hashKey + i + i * i) % range;
			buff.position((index) * recordSize);
			mask = buff.getInt();
			currentPos = (mask & 1) == 1;
		}
		if (inTable)
			row = getRecord(index);
		return row;
	}

	@Override
	public boolean remove(Object key) {
		var hashKey = toHash(key);
		buff.position(hashKey * recordSize);
		int mask = buff.getInt();
		boolean currentPos = false;
		boolean inTable = false;
		currentPos = (mask & 1) == 1;
		int i = 0;
		int index = hashKey;
		
		while (currentPos && i < range) {
			var temp = getRecord(index);
			if (key.equals(temp.get(primaryIndex))) {
				inTable = true;
				break;
			}
			i++;
			index = (hashKey + i + i * i) % range;
			buff.position((index) * recordSize);
			mask = buff.getInt();
			currentPos = (mask & 1) == 1;
		}
		if (inTable) {
			var arr = new byte[recordSize];
			buff.position(index * recordSize);
			buff.put(arr);
			size--;
			writeSize();
		}
		return inTable;
	}
	
	public int base (int hashIn) {
		return Math.floorMod(hashIn, capacity());
	}


	public int toHash(Object key) {
		if (key instanceof String) {
			
			int hashSum = 0;
			int charToA;
			
			for (var i = 0; i < ((String) key).length(); i++) {
				charToA = ((String) key).charAt(i);
				hashSum = hashSum + charToA;
			}
			int end = ((String) key).charAt(((String) key).length() - 1);
			return base(hashSum * end);
		} else
			return base(key.hashCode());
	}


	public boolean primeCheck(int num) {
		if (num % 2 == 0 || num % 3 == 0)
			return false;
		for (int i = 5; i * i <= num; i = i + 6) {
			if (num % i == 0 || num % (i + 2) == 0)
				return false;
		}
		return true;
	}
	
	
	public void writeFile() {
		Set<StandardOpenOption> set = new HashSet<StandardOpenOption>();

		set.add(StandardOpenOption.WRITE);
		set.add(StandardOpenOption.READ);

		try {
			file = FileChannel.open(Paths.get("data", tableName), set);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void writeHeader() {
		Set<StandardOpenOption> set = new HashSet<StandardOpenOption>();

		set.add(StandardOpenOption.CREATE);
		set.add(StandardOpenOption.WRITE);
		set.add(StandardOpenOption.READ);
		set.add(StandardOpenOption.TRUNCATE_EXISTING);

		try {
			var direct = new File("data");
			if (!direct.exists()) {
				direct.mkdir();
			}
			file = FileChannel.open(Paths.get("data", tableName), set);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	



	public void writeSchema() {
		try {
			buff = file.map(MapMode.READ_WRITE, 0, bitmaskHeader());
			buff.putInt(bitmaskHeader());
			buff.putInt(bitmaskRecord());
			buff.putInt(range);
			buff.putInt(size);
			buff.putInt(columnNum);
			buff.put(toString(tableName));

			for (var i = 0; i < columnNum; i++)
				buff.put(toString(columnNames.get(i)));

			for (var i = 0; i < columnNum; i++)
				buff.put(toString(columnTypes.get(i)));

			buff.putInt(primaryIndex);
			recordSize = bitmaskRecord();
			headerSize = bitmaskHeader();
			buff = file.map(MapMode.READ_WRITE, bitmaskHeader(), bitmaskRecord() * range);
		}

		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Object read(String object) {
		Object value = null;

		switch (object) {

		case "string":
			var arr = new byte[127];
			buff.get(arr);
			value = new String(arr).trim();
			break;
			
		case "integer":
			value = buff.getInt();
			break;

		case "boolean":
			char value2 = 0;
			value2 = (char) buff.get();
			value = (value2 == 't');
			break;
		}
		return value;
	}
	
	public void isNull(String object) {

		switch (object) {

		case "string":
			var arr = new byte[127];
			buff.get(arr);
			break;
			
		case "integer":
			buff.getInt();
			break;

		case "boolean":
			buff.get();
			break;
		}
	}
	



	public List<Object> getRecord(int index) {
		buff.position(index * recordSize);
		int bMask = buff.getInt();
		List<Object> row = new LinkedList<Object>();

		if ((bMask & 1) == 1) {
			
			for (int i = 0; i < columnTypes.size(); i++) {
				
				if ((bMask & (1 << (i + 1))) != 0)
					row.add(read(columnTypes.get(i)));
				
				else {
					row.add(null);
					isNull(columnTypes.get(i));
				}
			}
		}
		return row;
	}



	public void writeSize() {

		try {
			var buffer = file.map(MapMode.READ_WRITE, 12, 16);
			buffer.putInt(size);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void writeRecord(int index, List<Object> row) {
		buff.position(recordSize * index + 4);
		int mask = 1;

		for (var i = 0; i < columnNum; i++) {
			var object = columnTypes.get(i);
			if (row.get(i) == null)
				writeNull(object);
			else {
				write(row.get(i), object);
				mask = mask | (2 << i);
			}
		}

		buff.position(recordSize * index);
		buff.putInt(mask);
	}
	
	public void writeNull(String object) {

		switch (object) {

		case "string":
			buff.put(readString2(""));
			break;
			
		case "integer":
			buff.putInt(0);
			break;

		case "boolean":
			buff.put((byte) 0);
			break;
		}
	}

	public void write(Object value, String object) {
		switch (object) {


		case "string":
			buff.put(readString2((String) value));
			break;
			
		case "integer":
			buff.putInt((int) value);
			break;

		case "boolean":
			var b = (Boolean) value;
			buff.put((byte) b.toString().charAt(0));
			break;
			
		}
	}
	

	public int bitmaskRecord() {
		var bitMaskLength = 0;
		bitMaskLength = bitMaskLength + 4;
		for (String object : columnTypes) {
			if (object.equals("integer")) {
				bitMaskLength = bitMaskLength + 4;
			} else if (object.equals("boolean")) {
				bitMaskLength = bitMaskLength + 1;
			} else {
				bitMaskLength = bitMaskLength + 127;
			}
		}
		return bitMaskLength;
	}

	public int bitmaskHeader() {
		int maskLength = 0;
		//had to add comments to help me tell the difference
		maskLength = maskLength + 4; // header
		maskLength = maskLength + 4; //record
		maskLength = maskLength + 4; // range
		maskLength = maskLength + 4; // size
		maskLength = maskLength + 4; // column number
		maskLength = maskLength + 15; // table name
		maskLength = maskLength + 4; // primary index
		maskLength += columnNames.size() * 15;
		maskLength += columnTypes.size() * 15;
		return maskLength;
	}

	public String readString1() {
		var bArr = new byte[15];
		buff.get(bArr);

		var string1 = new String(bArr);

		return string1.trim();
	}

	public byte[] readString2 (String string) {

		var byteArray = new byte[127];
		System.arraycopy(string.getBytes(), 0, byteArray, 127 - string.length(), string.length());
		return byteArray;
	}

	public byte[] toString(String string) {

		var byteArray = new byte[15];
		System.arraycopy(string.getBytes(), 0, byteArray, 15 - string.length(), string.length());
		return byteArray;
	}

@Override
public Iterator<List<Object>> iterator() {
	return new SecondIterator();
}

public class SecondIterator implements Iterator<List<Object>> {
	int index = 0;
	public MappedByteBuffer buffer;

	public SecondIterator() {
		try {
			buffer = file.map(MapMode.READ_ONLY, headerSize, recordSize * range);
		} catch (IOException e) {
			e.printStackTrace();
		}
		nextEmpty();
	}

	@Override
	public List<Object> next() {
		List<Object> row = new LinkedList<Object>();

		buffer.position(recordSize * index);
		int mask = buffer.getInt();

		for (var i = 0; i < columnNum; i++) {
			String object = columnTypes.get(i);
			if ((mask & (2 << i)) == 0)
				row.add(null);
			else {
				switch (object) {
				case "string":
					var byteArr = new byte[127];
					buffer.get(byteArr);
					var t = new String(byteArr);
					row.add(t.trim());
					break;
					
				case "integer":
					row.add(buffer.getInt());
					break;
					
				case "boolean":
					var val = (char) buffer.get();
					row.add(val == 't');
					break;
				}
			}
		}
		index++;
		nextEmpty();
		return row;
	}
	
	@Override
	public boolean hasNext() {
		return index < range;
	}

	public void nextEmpty() {
		while (index < range && isEmpty(index))
			index++;
	}
	
	public boolean isEmpty(int index) {
		buffer.position(recordSize * index);
		var mask = buffer.getInt();
		return (mask & 1) == 0;
	}
}

}