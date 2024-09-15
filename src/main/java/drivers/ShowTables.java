package drivers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apps.Database;
import tables.SearchTable;
import tables.Table;

public class ShowTables implements Driver {
	static final Pattern pattern = Pattern.compile(
		"show\\s+tables\\s*",
		Pattern.CASE_INSENSITIVE
	);
	
	public Object execute(String query, Database db) {
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches())
			return null;
		
	
		
		Table result_Table = new SearchTable(
				"_tables",
				List.of("table_name", "column_count", "row_count"),
				List.of("string", "integer", "integer"),
				0
				);
		
		for(int i = 0; i< db.tables().size(); i++) {
			result_Table.put(List.of(db.tables().get(i).getTableName(), db.tables().get(i).getColumnNames().size(), db.tables().get(i).size()));
		}
		
		return result_Table;
		
		
		
		
		}
}
	

