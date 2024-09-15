package drivers;



import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apps.Database;

public class DropTable implements Driver {
	static final Pattern pattern = Pattern.compile(
		"DROP\s+TABLE\s+([a-z][a-z0-9_]*)",
		Pattern.CASE_INSENSITIVE
	);
	
	public Object execute(String query, Database db) {
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches())
			return null;
		
		String table_name = matcher.group(1);
		
		if (!db.exists(table_name)) {
			return new SQLError("Table <%s> does not exist".formatted(table_name));
		}
		else {
			int numRows = db.find(table_name).size();
			db.drop(table_name);
			return numRows;
		}
}
	
}
