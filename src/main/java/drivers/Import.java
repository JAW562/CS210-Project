package drivers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import apps.Database;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import tables.SearchTable;
import tables.Table;

public class Import implements Driver {
	static final Pattern pattern = Pattern.compile(
			"import\\s+([a-z][a-z0-9_]{0,14}\\.(?:xml|json))(?:\\s+to\\s+([a-z][a-z0-9_]{0,14}))?",
			Pattern.CASE_INSENSITIVE);

	@Override
	public Object execute(String query, Database db) {
		var matcher = pattern.matcher(query.strip());
		if (!matcher.matches())
			return null;

		String fileName = matcher.group(1);
		String tableName = matcher.group(2);
		Table table = null;
		
		
		if (fileName.endsWith(".xml")) {
			table = readXML(fileName, tableName, db);
		} else {
			table = readJson(fileName, tableName, db);
		}
		db.create(table);
		Table resultTable = new SearchTable(table.getTableName(), table.getColumnNames(), table.getColumnTypes(),
				table.getPrimaryIndex());
		for (List<Object> row : table.rows()) {
			resultTable.put(row);
		}
		return resultTable;
	}

	private static Table readJson(String filename, String table_name, Database db) {
		JsonReader reader = null;
		Table resultTable = null;
		try {
			reader = Json.createReader(new FileInputStream(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		JsonObject rObject = reader.readObject();
		reader.close();

		JsonObject schema = rObject.getJsonObject("schema");
		if (table_name == null) {
			table_name = schema.getString("table_name");
		}
		
		int i =1;
		
		while (db.exists(table_name)) {
			table_name = table_name +i;
			i++;
		}
		
		int primaryIndex = schema.getInt("primary_index");
		JsonArray columnTypes = schema.getJsonArray("column_types");
		JsonArray columnNames = schema.getJsonArray("column_names");
		List<String> columnTypesList = new ArrayList<String>();
		List<String> columnNamesList = new ArrayList<String>();
		for (int z = 0; z < columnNames.size(); z++) {
			columnTypesList.add(columnTypes.getString(z));
			columnNamesList.add(columnNames.getString(z));
		}
		resultTable = new SearchTable(table_name, columnNamesList, columnTypesList, primaryIndex);

		JsonArray state = rObject.getJsonArray("state");
		for (int j = 0; j < state.size(); j++) {
			JsonArray row = state.getJsonArray(j);
			List<Object> list = new ArrayList<Object>();
			for (int k = 0; k < row.size(); k++) {
				String type = columnTypesList.get(k);
				String value = row.getString(k);
				if (type.equalsIgnoreCase("integer")) {
					list.add(Integer.parseInt(value));
				} else if (type.equalsIgnoreCase("boolean")) {
					list.add(Boolean.parseBoolean(value));
				} else {
					list.add(value);
				}
			}
			resultTable.put(list);
		}
		return resultTable;
	}
	
//	
//	if (db.exists(tableName)) {
//		tableName = tableName + "_1";
//	}

	private static Table readXML(String filename, String tableName, Database db) {
		Table resultTable = null;
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(filename));
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Element rObject = doc.getDocumentElement();
		rObject.normalize();

		Node tableNames = doc.getElementsByTagName("tableName").item(0);
		if (tableName == null) {
			tableName = tableNames.getTextContent();
		}
		
	int l =1;
		
		while (db.exists(tableName)) {
			tableName= String.format("%s_%d", tableName, l);
			l++;
		}
		
		
		
		Node primaryIndex = doc.getElementsByTagName("primaryIndex").item(0);
		int in = Integer.parseInt(primaryIndex.getTextContent());

		NodeList names = doc.getElementsByTagName("name");
		List<String> columnNamesList = new ArrayList<String>();
		for (int i = 0; i < names.getLength(); i++) {
			columnNamesList.add(names.item(i).getTextContent());
		}

		NodeList type = doc.getElementsByTagName("type");
		List<String> columnTypesList = new ArrayList<String>();
		for (int i = 0; i < type.getLength(); i++) {
			columnTypesList.add(type.item(i).getTextContent());
		}

		resultTable = new SearchTable(tableName, columnNamesList, columnTypesList, in);

		NodeList rows = doc.getElementsByTagName("row");
		for (int i = 0; i < rows.getLength(); i++) {
			Element row = (Element) rows.item(i);
			NodeList data = row.getElementsByTagName("data");
			List<Object> en = new ArrayList<Object>();
			for (int k = 0; k < data.getLength(); k++) {
				String value = data.item(k).getTextContent();
				if (columnTypesList.get(k).equalsIgnoreCase("Integer")) {
					en.add((value));
				} else if (columnTypesList.get(k).equalsIgnoreCase("Boolean")) {
					en.add(Boolean.parseBoolean(value));
				} else {
					en.add(value);
				}
			}
			resultTable.put(en);
		}
		return resultTable;
	}
}