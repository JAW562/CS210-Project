package drivers;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apps.Database;
import tables.SearchTable;
import tables.Table;

/**
 * Driver for execution of named macros
 * for testing or grading purposes.
 * <p>
 * Modify the macros for your use case.
 */
@Deprecated
public class Macros implements Driver {
	static final Pattern pattern = Pattern.compile(
		"MACRO\\s+(\\w+)(?:\\s+(.+))?",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public Object execute(String query, Database db) throws SQLError {
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) return null;

		String macro = matcher.group(1);
		String option = matcher.group(2) != null ? matcher.group(2).strip() : null;

		/*
		 * Example:
		 *   MACRO 1
		 *
		 * Result:
		 *   table: macro_1 with 3 columns by 7 rows
		 *   [table also injected into database]
		 */
		if (macro.equals("1")) {
			Table table = new SearchTable(
				"macro_1",
				List.of("letter", "order", "vowel"),
				List.of("string", "integer", "boolean"),
				0
			);
			table.put(List.of("alpha", 1, true));
			table.put(List.of("beta", 2, false));
			table.put(List.of("gamma", 3, false));
			table.put(List.of("delta", 4, false));
			table.put(List.of("tau", 19, false));
			table.put(List.of("pi", 16, false));
			table.put(List.of("omega", 24, true));

			db.create(table);

			return table;
			
		}
		
		
		
		if (macro.equals("3")) {
			Table table = new SearchTable(
				"macro_3",
				List.of("order", "letter", "vowel"),
				List.of("integer", "string", "boolean"),
				1
			);
			table.put(Arrays.asList(1, "aplhaaaaaa", null));
			table.put(List.of(2, "beta", false));
			table.put(List.of(4, "delta", false));
			table.put(List.of(3, "gamma", false));
			table.put(List.of(19, "tau", false));
			table.put(List.of(0, "pi", false));
			table.put(Arrays.asList(24,"omegaaaaaa" , null));

			db.create(table);

			return table;
		}
		
		
		
		if (macro.equals("4")) {
			Table table = new SearchTable(
				"macro_4",
				List.of("letter", "order", "vowel"),
				List.of("string", "integer", "boolean"),
				0
			);
			table.put(List.of("a", 1, true));
			table.put(List.of("b", 0, false));
			table.put(List.of("h", 3, false));
			table.put(List.of("n", 14, true));
			table.put(List.of("k", 9, false));
			table.put(List.of("s", 11, false));
			table.put(List.of("v", 27, true));

			db.create(table);

			
			return table;
		}
		
		

		/*
		 * Example:
		 *   MACRO 2
		 *
		 * Result:
		 *   result set: macro_2 renamed from nested query
		 *   [table not injected into database]
		 */
		if (macro.equalsIgnoreCase("2")) {
			Table result_set = (Table) db.interpret("RANGE 10 AS x");
			result_set.setTableName("macro_2");

			return result_set;
		}

		/*
		 * Example:
		 *   MACRO SIZEOF macro_1
		 *
		 * Result:
		 *   integer (affected rows): 7
		 *   [number of rows in given table]
		 */
		if (macro.equalsIgnoreCase("SIZEOF")) {
			Table table = (Table) db.interpret("SHOW TABLE %s".formatted(option));

			return table.size();
		}

		/*
		 * Example:
		 *   MACRO RESET
		 *
		 * Result:
		 *   integer (affected rows): varies
		 *   [all tables dropped from database]
		 */
		if (macro.equalsIgnoreCase("RESET")) {
			int sum = 0;

			for (Table table: db.tables()) {
				sum += table.size();
				db.drop(table.getTableName());
			}

			return sum;
		}

		throw new SQLError("Macro <%s> is undefined".formatted(macro));
	}
}
