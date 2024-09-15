package drivers;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import apps.Database;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;
import tables.SearchTable;
import tables.Table;


public class Export implements Driver {
	static final Pattern pattern = Pattern.compile(
			"export\\s+([a-z][a-z0-9_]{0,14})(?:\\s+to\\s+((?:[a-z][a-z0-9_]{0,14}\\.(?:xml|json)))|\\s+as\\s+(xml|json))?",
			Pattern.CASE_INSENSITIVE);

	@Override
	public Object execute(String query, Database db) {
		var matcher = pattern.matcher(query.strip());
		if (!matcher.matches())
			return null;

		String tableName = matcher.group(1);
		String fileName = matcher.group(2);
		String format = matcher.group(3);

		if (!db.exists(tableName)) {
			return new SQLError( "Table does not exist");
		} else {
			Table table = db.find(tableName);
			if (fileName == null) {
				fileName = tableName + "." + format;
			}
			if (fileName.endsWith("xml")) {
				writeXML(fileName, table);
			} else {
				writeJson(fileName, table);
			}
			Table resultTable = new SearchTable("_export", table.getColumnNames(), table.getColumnTypes(),
					table.getPrimaryIndex());
			for (List<Object> row : table.rows()) {
				resultTable.put(row);
			}
			return true;
		}
	}

	private void writeJson(String filename, Table inputTable) {
		JsonObjectBuilder rObjectBuilder = Json.createObjectBuilder();
		JsonObjectBuilder schemaBuilder = Json.createObjectBuilder();

		schemaBuilder.add("table_name", inputTable.getTableName());

		JsonArrayBuilder columnNamesBuilder = Json.createArrayBuilder();
		for (String name : inputTable.getColumnNames()) {
			columnNamesBuilder.add(name);
		}

		schemaBuilder.add("column_names", columnNamesBuilder.build());

		JsonArrayBuilder columnTypesBuilder = Json.createArrayBuilder();
		for (int i =0; i < inputTable.getColumnTypes().size(); i++) {
			columnTypesBuilder.add(inputTable.getColumnTypes().get(i));
		}
		
		schemaBuilder.add("column_types", columnTypesBuilder.build());

		schemaBuilder.add("primary_index", inputTable.getPrimaryIndex());
		rObjectBuilder.add("schema", schemaBuilder.build());

		JsonArrayBuilder stateBuilder = Json.createArrayBuilder();
		for (List<Object> row : inputTable.rows()) {
			JsonArrayBuilder rowBuilder = Json.createArrayBuilder();
			for(int i = 0; i<row.size(); i++) {
				 String type = inputTable.getColumnTypes().get(i); 
					 
				 if(row.get(i)== null) {
					 rowBuilder.addNull();
				 } else if(type.equalsIgnoreCase("string")) {
					 rowBuilder.add(row.get(i).toString());
				 } else if (type.equalsIgnoreCase("boolean")) {
					 rowBuilder.add((boolean)row.get(i));
				 } else if(type.equalsIgnoreCase("integer")) {
					 rowBuilder.add((Integer)row.get(i));
				 }
				 
				 }
			stateBuilder.add(rowBuilder.build());

		}
		rObjectBuilder.add("state", stateBuilder.build());

		JsonObject rObject = rObjectBuilder.build();

		JsonWriterFactory fact = Json.createWriterFactory(Map.of(JsonGenerator.PRETTY_PRINTING, true));
		JsonWriter write = null;
		try {
			write = fact.createWriter(new FileOutputStream(filename));
		} catch (FileNotFoundException e) {
		}
		write.writeObject(rObject);
		write.close();
	}

	private void writeXML(String filename, Table inputTable) {

		Document doc = null;
		DocumentBuilder builder = null;
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		try {
			builder = fact.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		doc = builder.newDocument();

		Element root = doc.createElement("table");
		doc.appendChild(root);

		Element schema = doc.createElement("schema");
		root.appendChild(schema);

		Element tableName = doc.createElement("tableName");
		tableName.setTextContent(inputTable.getTableName());
		schema.appendChild(tableName);

		Element columnNames = doc.createElement("columnNames");
		schema.appendChild(columnNames);

		for (String name : inputTable.getColumnNames()) {
			Element names = doc.createElement("name");
			names.setTextContent(name);
			columnNames.appendChild(names);
		}

		Element columnTypes = doc.createElement("columnTypes");
		schema.appendChild(columnTypes);

		for (String type : inputTable.getColumnTypes()) {
			Element types = doc.createElement("type");
			types.setTextContent(type);
			columnTypes.appendChild(types);
		}

		Element primaryIndex = doc.createElement("primaryIndex");
		primaryIndex.setTextContent(inputTable.getPrimaryIndex() + "");
		schema.appendChild(primaryIndex);

		Element state = doc.createElement("state");
		root.appendChild(state);

		for (List<Object> row : inputTable.rows()) {
			Element rows = doc.createElement("row");
			for (Object info : row) {
				Element inform = doc.createElement("data");
				if (info == null) {
					info = "null";
				}
				inform.setTextContent(info.toString());
				rows.appendChild(inform);
			}
			state.appendChild(rows);
		}

		Source comesFrom = new DOMSource(doc);
		Result goesTo = null;
		try {
			goesTo = new StreamResult(new FileWriter(filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Transformer tForm = null;
		try {
			tForm = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}
		tForm.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		tForm.setOutputProperty(OutputKeys.INDENT, "yes");
		try {
			tForm.transform(comesFrom, goesTo);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
}



	



