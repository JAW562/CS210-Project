package drivers;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apps.Database;
import tables.SearchTable;
import tables.Table;

/*
 * Examples:
 * 	 SQUARES BELOW 20
 * 	 SQUARES BELOW 30 AS a
 * 	 SQUARES BELOW 15 AS a, b
 *
 * Result 1:
 *   result set:
 * 	   primary integer column "x", integer column "x_squared"
 *	   rows [0, 0]; [1, 1]; [2, 4]; [3, 9]; [4, 16]
 *
 * Result 2:
 *   result set:
 * 	   primary integer column "a", integer column "a_squared"
 *	   rows [0, 0]; [1, 1]; [2, 4]; [3, 9]; [4, 16]; [5, 25]
 *
 * Result 3:
 *   result set:
 * 	   primary integer column "a", integer column "b"
 *	   rows [0, 0]; [1, 1]; [2, 4]; [3, 9]
 */
@Deprecated
public class SquaresBelow implements Driver {
	static final Pattern pattern = Pattern.compile(
			"SQUARES\\s+BELOW\\s+([0-9]+)(?:\\s+AS\\s+([a-z][a-z0-9_]*))?(?:\\s*,\\s*([a-z][a-z0-9_]*))?",
			//SQUARES\\s+BELOW\\s+([0-9]+)(?:\\s+AS\\s+([a-z][a-z0-9_]*))?
			//SQUARES\\s+BELOW\\s+([0-9]+)(?:\\s+AS\\s+([a-z][a-z0-9_]*))?(?:\\s+,\\s*([a-z][a-z0-9_]*))?
			Pattern.CASE_INSENSITIVE
		);

		@Override
		public Object execute(String query, Database db) throws SQLError {
			Matcher matcher = pattern.matcher(query.strip());
			if (!matcher.matches()) return null;

			int upper = Integer.parseInt(matcher.group(1));
			String name = matcher.group(2) != null ? matcher.group(2) : "x";
			String secondName = matcher.group(3) !=null ? matcher.group(3) :name +"_squared";
			if(name.equals(secondName)) {
				return new SQLError("Duplicate name not allowed");
			}

			Table result_set = new SearchTable(
				"_squares",
				List.of(name,secondName),
				List.of("integer", "integer"),
				0
			);

			for (int i = 0; i < upper; i++) {
//				result_set.put(List.of(i));
				List<Object> row = new LinkedList<>();
				int q = (int)(Math.pow(i, 2));
				if(q<upper) {
				row.add(i);
				row.add(q);
				result_set.put(row);
			}

			}

			return result_set;
		}
	}

