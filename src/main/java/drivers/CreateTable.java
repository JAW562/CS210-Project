/**
 * 
 */
package drivers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apps.Database;
import tables.SearchTable;
import tables.Table;

/**
 * @author Jared Willis
 *
 */
	
		public class CreateTable implements Driver {
			static final Pattern pattern = Pattern.compile(
				"CREATE\\s+TABLE\\s+([a-z][a-z0-9_]*)\\s*\\((\\s*[a-z][a-z0-9_]*\\s+(?:STRING|INTEGER|BOOLEAN)\\s*(?:PRIMARY)?\\s*(?:\\s*,\\s*[a-z][a-z0-9_]*\\s+(?:STRING|INTEGER|BOOLEAN)\\s*(?:PRIMARY)?)*)\\)",
				Pattern.CASE_INSENSITIVE
			);
			// CREATE\\s+TABLE\\s+([a-z][a-z0-9_]*)\\s*\\((\\s*[a-z][a-z0-9_]*\\s+(?:STRING|INTEGER|BOOLEAN)\\s*(?:PRIMARY)?\\s*(?:\\s*,\\s*[a-z][a-z0-9_]*\\s+(?:STRING|INTEGER|BOOLEAN)\\s*(?:PRIMARY)?)*)\\)
			@Override
			public Object execute(String query, Database db) {
				Matcher matcher = pattern.matcher(query.strip());
				if (!matcher.matches())
					return null;
				
				String tableName = matcher.group(1);
				String columns = matcher.group(2);
				
				if(db.exists(tableName)) {
					return new SQLError("Tables name already exsits");
				}
				
				if(tableName.length()>15){
					return new SQLError("Table name is too long");
				}
				
				List<String> columnNames = new ArrayList<String>();
				List<String> columnTypes = new ArrayList<String>();
				int primaryIndex = -1;
				
				columns = columns.strip();
				
				String[] columnDF= columns.split("\s*,\s*");
				
				for(int i = 0; i<columnDF.length; i++) {
					String[] columnDef= columnDF[i].split("\s+");
					if(columnDef.length==3 && columnDef[2].equalsIgnoreCase("PRIMARY")) {
						if(primaryIndex==-1) {
							primaryIndex = i;
						}
						else {
							return new SQLError("Primary column already exsits");
						}
					}
					if(columnNames.contains(columnDef[0])){
						return new SQLError("Duplicate column names are not allowed");
					}
					if(columnDef[0].length()>15) {
						return new SQLError("Column name cannot be longer than 15 characters");
					}
					columnNames.add(columnDef[0]);
					columnTypes.add(columnDef[1].toLowerCase());
				}
				
				if(primaryIndex==-1) {
					return new SQLError("A primary column was never defined");
				}
				
				if(columnNames.size()>15) {
					return new SQLError("There cannot be more than 15 columns");
				}
				

				Table fin = new SearchTable(
						tableName,
						columnNames,
						columnTypes,
						primaryIndex
						);
				
				db.create(fin);
				
				return fin;
				
		
			}
		
	}



