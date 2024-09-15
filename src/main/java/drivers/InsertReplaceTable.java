package drivers;



import java.util.LinkedList;

import java.util.List;

import java.util.regex.Matcher;

import java.util.regex.Pattern;



import apps.Database;

import tables.Table;



public class InsertReplaceTable implements Driver {

	static final Pattern pattern = Pattern.compile(

			"(INSERT|REPLACE)\\s+INTO\\s+([a-z0-9]+[a-z0-9_]+)\\s+(?:\\(([^()]*)\\)\\s+)?VALUES\\s+\\(([^()]*)\\)",



			// "(INSERT|REPLACE)\\s+INTO\\s+([a-z][a-z0-9_]*)\\s*(?:\\(([^)]+)\\))?\\s*VALUES\\s*\\(([^)]+)\\)",

			Pattern.CASE_INSENSITIVE);



	// (INSERT|REPLACE)\s+INTO\s+([a-z][a-z0-9_]*)\s*(?:\(([^)]+)\))?\s*VALUES\s*\(([^)]+)\)



	static final Pattern stringPattern = Pattern.compile("\"(?:.+)?\"", Pattern.CASE_INSENSITIVE);



	static final Pattern integerPattern = Pattern.compile("[0-9+-.]*", Pattern.CASE_INSENSITIVE);



	static final Pattern booleanPattern = Pattern.compile("(TRUE|FALSE)", Pattern.CASE_INSENSITIVE);



	static final Pattern nullPattern = Pattern.compile("NULL", Pattern.CASE_INSENSITIVE);



	@Override

	public Object execute(String query, Database db) {
		
		int counter=0;

		Matcher matcher = pattern.matcher(query.strip());

		if (!matcher.matches()) {

			return null;

		}



		String mode = matcher.group(1).toLowerCase();

		String tableName = matcher.group(2);

		String columns = matcher.group(3);

		String data = matcher.group(4);



		if (!db.exists(tableName)) {

			return new SQLError("Table name already exsists");

		}



		Table table = db.find(tableName);



		List<Integer> pointersList = new LinkedList<Integer>();



		if (columns == null) {

			for (int i = 0; i < table.getColumnNames().size(); i++) {

				pointersList.add(i);

			}

		} else {



			if (columns.length() < 1) {

				return new SQLError("Cannot have empty columns");

			}



			String[] columnSplit = columns.strip().split("\\s*,\\s*");

			for (int i = 0; i < columnSplit.length; i++) {

				columnSplit[i] = columnSplit[i].replace(')', ' ');

				columnSplit[i] = columnSplit[i].replace('(', ' ');

				columnSplit[i] = columnSplit[i].strip();

				if (!columnSplit[i].matches("([a-z0-9][a-z0-9_]*)") || columnSplit[i].length() > 15) {

					return new SQLError("There is an issue with the column names.");

				}

			}

			boolean primaryFlag = false;



			for (int i = 0; i < columnSplit.length; i++) {

				String name = columnSplit[i].strip();

				int j = table.getColumnNames().indexOf(name);



				if (j == -1) {

					return new SQLError("Unknown column names");

				}



				if (pointersList.contains(j)) {

					return new SQLError("Duplicate column name");

				}

				pointersList.add(j);

				if (table.getPrimaryIndex()==j) {

					primaryFlag = true;

				}



			}

			if (!primaryFlag) {

				return new SQLError("No primary");



			}

		}



		List<Object> newRow = new LinkedList<Object>();

		Object processedValue = null;



		for (int i = 0; i < table.getColumnNames().size(); i++) {

			newRow.add(null);

		}



		String[] dataSplit = data.strip().split("\\s*,\\s*");

		for (int i = 0; i < dataSplit.length; i++) {

			dataSplit[i] = dataSplit[i].replace(')', ' ');

			dataSplit[i] = dataSplit[i].replace('(', ' ');

			dataSplit[i] = dataSplit[i].strip();



		}



		// for(int i =0; i<dataSplit.length; i++) {

		// System.out.println(dataSplit[i]);

		// }

		// 

		System.out.println("Data split " + dataSplit.length);

		System.out.println("Pointers List " + pointersList.size());

		if (dataSplit.length == 0) {

			return new SQLError("No data");

		} else if (dataSplit.length < pointersList.size()) {

			return new SQLError("Less than pointers list");

		} else if (dataSplit.length > pointersList.size()) {

			return new SQLError("More than pointers list");

		}



		if (dataSplit.length == pointersList.size()) {

			for (int i = 0; i < dataSplit.length; i++) {

				String rawValue = dataSplit[i].toString();

				int j = pointersList.get(i);

				String exceptedType = table.getColumnTypes().get(j).toLowerCase();



				Matcher strMatcher = stringPattern.matcher(rawValue);



				if (strMatcher.matches()) {

					if (!(exceptedType.equalsIgnoreCase("String"))) {

						return new SQLError("String error");

					}

					if (rawValue.length() > 129) {

						return new SQLError("String is too long");

					}



					processedValue = rawValue.substring(1, rawValue.length() - 1);

					System.out.println(rawValue);



				}

				Matcher intMatcher = integerPattern.matcher(rawValue);

				// else if raw value matches an integer pattern

				//	else if (rawValues[i].matches("\\-?\\d+")) {



				if (intMatcher.matches()) {

					if (!dataSplit[i].equals("0")) {

						if (dataSplit[i].startsWith("0")) {

							return new SQLError("Leading zeros are not allowed");

						}

					}

					if (dataSplit[i].matches("\\d*\\.\\d+|\\d+\\.\\d*")) {

						return new SQLError("Fractions are not allowed");

					}

					if (!(exceptedType.equalsIgnoreCase("INTEGER"))) {

						return new SQLError("Integer error");

					}

					try {

						processedValue = Integer.parseInt(rawValue);

					} catch (NumberFormatException pIR) {

						return new SQLError("NumberFormatException");

					}

				}



				Matcher bMatcher = booleanPattern.matcher(rawValue);

				if (bMatcher.matches()) {

					if (!(exceptedType.equalsIgnoreCase("BOOLEAN"))) {

						return new SQLError("Boolean error");

					}

					processedValue = Boolean.parseBoolean(rawValue);

				}



				Matcher nMatcher = nullPattern.matcher(rawValue);

				if (nMatcher.matches()) {

					processedValue = null;

				}



				if (table.getPrimaryIndex()==j) {

					Object key = processedValue;



					if (key == null) {

						return new SQLError("Key equals null");

					}



					if (mode.equalsIgnoreCase("REPLACE")) {

						newRow.set(j, key);



					} else if (mode.equalsIgnoreCase("INSERT")) {

						if (!table.contains(key)) {



							newRow.set(j, key);



						} else {

							return new SQLError("Duplicate key");

						}

					}

				} else {

					newRow.set(j, processedValue);

				}

			}

		}
		
		table.put(newRow);
		counter++;

		return (counter);
	}
}
