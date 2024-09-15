package drivers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import apps.Database;
import tables.HashArrayTable;
import tables.Table;

public class SelectFromTable implements Driver {
	static final Pattern pattern = Pattern.compile(
			"select\\s+(\\*|(?:\\s*(?:[a-z][a-z0-9_]{0,14}(?:\\s+as\\s+[a-z][a-z0-9_]{0,14})?)(?:\\s*,\\s*(?:[a-z][a-z0-9_]{0,14}(?:\\s+as\\s+[a-z][a-z0-9_]{0,14})?))*))\\s+from\\s+([a-z][a-z0-9_]{0,14})(?:\\s+where\\s+([a-z][a-z0-9_]{0,14})\\s*(=|<>|<|>|<=|>=)\\s*((?:\\\"(?:\\w{0,127})\\\")|(?:0|[+-]?[1-9]\\d*)|(?:true|false)|null))?",
			Pattern.CASE_INSENSITIVE);

	@Override
	public Object execute(String query, Database db) {
		var matcher = pattern.matcher(query.strip());
		if (!matcher.matches())
			return null;

		String columnNames = matcher.group(1);
		String tableName = matcher.group(2);
		String lhs = matcher.group(3);
		String as = matcher.group(4);
		String rhs = matcher.group(5);
		boolean has = rhs != null;

		ArrayList<String> columnList = new ArrayList<String>();
		String[] columnArray = null;
		ArrayList<String> newColumnNames = new ArrayList<String>();

		if (!db.exists(tableName)) {
			return new SQLError("Table does not exist");
	
			
		} else {
			Table table = db.find(tableName);
			if (columnNames.equals("*")) {
				columnList.addAll(table.getColumnNames());
				newColumnNames.addAll(columnList);
			} else {
				columnArray = columnNames.split("\s*,\s*");
				for (int i = 0; i < columnArray.length; i++) {
					String[] temp = columnArray[i].split("(?i)\s+as\s+");
					columnList.add(temp[0]);
					if (temp.length == 2) {
						newColumnNames.add(temp[1]);
					} else {
						newColumnNames.add(temp[0]);
					}
				}
			}

			Set<String> set = new HashSet<String>();
			set.addAll(newColumnNames);

			if (set.size() != newColumnNames.size()) {
				return new SQLError("Duplicate column names not allowed");
			}

			ArrayList<String> columnTypes = new ArrayList<String>();
			List<String> ogNames = table.getColumnNames();
			List<String> ogTypes = table.getColumnTypes();
			HashMap<String, Integer> namesIndex = new HashMap<String, Integer>();
			HashMap<String, String> namesTypes = new HashMap<String, String>();

			for (int i = 0; i < ogNames.size(); i++) {
				namesTypes.put(ogNames.get(i), ogTypes.get(i));
				namesIndex.put(ogNames.get(i), i);
			}

			for (String name : columnList) {
				if (!namesTypes.containsKey(name)) {
					return new SQLError("This column does not exsist");
				}
				columnTypes.add(namesTypes.get(name));
			}

			String primaryName = ogNames.get(table.getPrimaryIndex());
			int primary = columnList.indexOf(primaryName);

			if (primary == -1) {
				return new SQLError("Primary was less than one");
			}
			Table result = new HashArrayTable("_select", newColumnNames, columnTypes, primary);

			for (List<Object> row : table.rows()) {
				ArrayList<Object> selectRow = new ArrayList<Object>();
				for (String name : columnList) {
					int index = namesIndex.get(name);
					selectRow.add(row.get(index));
				}
				if (has) {
					if (!namesIndex.containsKey(lhs)) {
						return new SQLError("Column does not exsist");
					}
					int lhs_index = namesIndex.get(lhs);
					String lhs_type = namesTypes.get(lhs);
					if (present(row.get(lhs_index), lhs_type, as, rhs)) {
						result.put(selectRow);
					}
				} else {
					result.put(selectRow);
				}
			}
			return result;
		}
	}

	private static boolean present(Object lhs, String lhsType, String as, Object rhs) {
		if (rhs == null || lhs == null || rhs.equals("null")) {
			return false;
		}
		int result = 0;
		Boolean con = false;
		if (lhsType.equals("string")) {
			String vals = (String) rhs;
			String val = (String) lhs;
			if (vals.startsWith("\"")) {
				vals = vals.substring(1, vals.length() - 1);
			}
			result = val.compareTo(vals);
		} else if (lhsType.equalsIgnoreCase("integer")) {
			Integer vals = null;
			Integer val = (Integer) lhs;
			try {
				vals = Integer.parseInt((String) rhs);
				result = val.compareTo(vals);
			} catch (NumberFormatException e) {
				String one = lhs.toString();
				String two = (String) rhs;
				result = one.compareTo(two);
			}
		} else {
			Boolean vals = (Boolean) lhs;
			Boolean val = null;
			if (rhs.equals("true") || rhs.equals("false")) {
				val = Boolean.parseBoolean((String) rhs);
				result = vals.compareTo(val);
			} else {
				String one = lhs.toString();
				String two = rhs.toString();
				result = one.compareTo(two);
			}
		}
		if (as.equals("=")) {
			con = (result == 0);
		} else if (as.equals("<>")) {
			con = (result != 0);
		} else if (as.equals("<")) {
			con = (result < 0);
		} else if (as.equals("<=")) {
			con = (result <= 0);
		} else if (as.equals(">")) {
			con = (result > 0);
		} else if (as.equals(">=")) {
			con = (result >= 0);
		}
		return con;
	}
}