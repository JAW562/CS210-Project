package apps;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import drivers.SQLError;

/**
 * Implements a user console for
 * interacting with a database.
 * <p>
 * Do not modify existing protocols,
 * but you may add new protocols.
 */
public class Console {
	/**
	 * The entry point for execution
	 * with user input/output.
	 *
	 * @param args unused.
	 */
	public static void main(String[] args) {
		try (
			final Database db = new Database(true);
			final Scanner in = new Scanner(System.in);
			final PrintStream out = System.out;
		) {
			
			while(true) {
			out.print(">> ");
			
			var input = in.nextLine().strip();
			
			if(input.startsWith("--"))
				continue;

			var script = input.split("\\s*;\\s*");
			for(var query:script) {
				query=query.strip();
				if(query.equalsIgnoreCase("EXIT"))
					return;
				out.println("Query: " + query);
				try {
					var result = db.interpret(query);
					if(result instanceof Integer rows) {
						out.println(rows + " many rows were affected.");
					} else {
						
					
					out.println("Result: " + result);
				}
				}
				catch (SQLError e) {
					out.println("Error: " + e);
				}
			}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
