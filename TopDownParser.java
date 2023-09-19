package sample;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TopDownParser {
	// Grammar rules
	private static Map<String, List<String>> grammar = new HashMap<>();

	// First sets
	private static Map<String, Set<String>> firstSets = new HashMap<>();

	// Follow sets
	private static Map<String, Set<String>> followSets = new HashMap<>();

	private static boolean isValidIdentifier(String identifier) {
		if (identifier == null || identifier.isEmpty()) {
			return false;
		}

		// Check if the first character is a letter
		char firstChar = identifier.charAt(0);
		if (!Character.isLetter(firstChar)) {
			return false;
		}

		// Check the remaining characters
		for (int i = 1; i < identifier.length(); i++) {
			char ch = identifier.charAt(i);
			if (!Character.isLetterOrDigit(ch) && ch != '_') {
				return false;
			}
		}

		return true;
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		// Add grammar rules
		grammar.put("G", Arrays.asList("S $"));
		grammar.put("S",
				Arrays.asList("select_statement", "drop_statement", "alter_statement", "truncate_statement",
						"grant_statement", "revoke_statement", "insert_statement", "update_statement",
						"delete_statement", "commit_statement", "rollback_statement"));
		grammar.put("select_statement", Arrays.asList("SELECT column_list FROM table_name WHERE condition"));
		grammar.put("drop_statement", Arrays.asList("DROP TABLE table_name"));
		grammar.put("alter_statement", Arrays.asList("ALTER TABLE table_name alter_action"));
		grammar.put("truncate_statement", Arrays.asList("TRUNCATE TABLE table_name"));
		grammar.put("grant_statement", Arrays.asList("GRANT privilege_list ON table_name TO user_name"));
		grammar.put("revoke_statement", Arrays.asList("REVOKE privilege_list ON table_name FROM user_name"));
		grammar.put("insert_statement", Arrays.asList("INSERT INTO table_name column_list VALUES value_list"));
		grammar.put("update_statement", Arrays.asList("UPDATE table_name SET column_name = value WHERE condition"));
		grammar.put("delete_statement", Arrays.asList("DELETE FROM table_name WHERE condition"));
		grammar.put("commit_statement", Arrays.asList("COMMIT"));
		grammar.put("rollback_statement", Arrays.asList("ROLLBACK"));
		grammar.put("column_list", Arrays.asList("column_name column_list_tail"));
		grammar.put("column_list_tail", Arrays.asList("ε", ", column_name column_list_tail"));
		grammar.put("alter_action",
				Arrays.asList("ADD column_definition", "DROP COLUMN column_name", "MODIFY column_definition"));
		grammar.put("column_definition", Arrays.asList("column_name data_type"));
		grammar.put("privilege_list", Arrays.asList("privilege privilege_list_tail"));
		grammar.put("user_name", Arrays.asList("'user1'"));
		grammar.put("privilege_list_tail", Arrays.asList("ε", ", privilege privilege_list_tail"));
		grammar.put("privilege", Arrays.asList("SELECT", "INSERT", "UPDATE", "DELETE"));
		grammar.put("table_name", Arrays.asList("identifier"));
		grammar.put("condition", Arrays.asList("expression comparison_operator expression"));
		grammar.put("expression", Arrays.asList("literal", "column_name"));
		grammar.put("comparison_operator", Arrays.asList("=", "<", ">", "<=", ">="));
		grammar.put("literal", Arrays.asList("' value '", "number", "true", "false", "'Electronics'"));
		grammar.put("column_name", Arrays.asList("identifier"));
		grammar.put("value_list", Arrays.asList("value value_list_tail"));
		grammar.put("value_list_tail", Arrays.asList("ε", ", value value_list_tail"));
		grammar.put("value", Arrays.asList("'value'", "number", "true", "false"));
		grammar.put("number", IntStream.range(1, 1000).mapToObj(Integer::toString).collect(Collectors.toList()));
		grammar.put("identifier", Arrays.asList("[a-zA-Z0-9_]+","accepted"));

		String tableName = "";
		List<String> tableNameList = new ArrayList<>();
		String identifier = "";

		System.out.print("Enter the number of table(s): ");
		int tableCount = scanner.nextInt();
		scanner.nextLine();

		for (int i = 1; i <= tableCount; i++) {
			do {
				System.out.print("Enter the table name #" + i + ": ");
				identifier = scanner.nextLine();

				if (isValidIdentifier(identifier)) {
					tableName = identifier;
					tableNameList.add(tableName);
					grammar.put("table_name", tableNameList);
					break;
				} else {
					System.out.println("Invalid table name. Please enter a valid identifier.");
				}
			} while (true);

			System.out.println("Accepted table name: " + tableName);
		}

		int z = 0;
		identifier = "";
		System.out.println("Does the statement include a change in the column?");
		System.out.println("1. Yes");
		System.out.println("2. No");
		z = scanner.nextInt();
		scanner.nextLine();
		String columnName = "";
		List<String> columnNameList = new ArrayList<>();

		if (z == 1) {
			System.out.print("Enter the number of column(s): ");
			int columnCount = scanner.nextInt();
			scanner.nextLine();

			for (int i = 1; i <= columnCount; i++) {
				do {
					System.out.print("Enter the column name #" + i + ": ");
					identifier = scanner.nextLine();

					if (isValidIdentifier(identifier)) {
						columnName = identifier;
						columnNameList.add(columnName);
						grammar.put("column_name", columnNameList);
						break;
					} else {
						System.out.println("Invalid column name. Please enter a valid identifier.");
					}
				} while (true);

				System.out.println("Accepted column name: " + columnName);
			}
		}

		computeFirstSets();
		computeFollowSets();

		// Generate parsing table
		Map<String, Map<String, List<String>>> parsingTable = generateParsingTable();

		// Print parsing table
		System.out.println("\nParsing Table:");
		for (String nonTerminal : grammar.keySet()) {
			System.out.println("Non-Terminal: " + nonTerminal);
			Map<String, List<String>> row = parsingTable.get(nonTerminal);
			for (String terminal : row.keySet()) {
				System.out.println(terminal + ": " + row.get(terminal));
			}
			System.out.println();
		}

		System.out.println("First Sets:");
		for (String nonTerminal : grammar.keySet()) {
			System.out.println(nonTerminal + ": " + firstSets.get(nonTerminal));
		}
		System.out.println();
		System.out.println("Follow Sets:");
		for (String nonTerminal : grammar.keySet()) {
			System.out.println(nonTerminal + ": " + followSets.get(nonTerminal));
		}
		
		System.out.print("Enter the SQL query : ");
		String input = scanner.nextLine();
		input = input + " $";
		System.out.println();
		
		/*
		 * SELECT column1 , column2 FROM table1 , table2 WHERE column2 = 50
		 * DELETE FROM customers WHERE age > 50
		 * DROP TABLE customers
		 * TRUNCATE TABLE logs
		 * GRANT SELECT , INSERT ON orders TO 'user1'
		 * REVOKE UPDATE , DELETE ON customers FROM 'user1'
		 * UPDATE products SET price = 10 WHERE category = 'Electronics'
		 * COMMIT
		 * ROLLBACK
		 */

		String[] inputTokens = input.split("\\s+");
		Stack<String> stack = new Stack<>();
		stack.push("G");

		int index = 0;
		boolean success = true;

		while (!stack.isEmpty() && index < inputTokens.length) {
			System.out.println(stack);
			String top = stack.peek();
			String currentToken = inputTokens[index];

			if (isTerminal(top)) {
				if (top.equals(currentToken)) {
					stack.pop();
					index++;
				} else {
					success = false;
					break;
				}
			} else {
				List<String> production = parsingTable.get(top).get(currentToken);
				if (production != null) {
					stack.pop();
					if (!production.get(0).equals("ε")) {
						for (int i = production.size() - 1; i >= 0; i--) {
							stack.push(production.get(i));
						}
					}
				} else {
					success = false;
					break;
				}
			}
		}

		if (!stack.isEmpty() || index < inputTokens.length) {
			success = false;
		}

		System.out.println("\n----------------Parsing Result----------------");
		System.out.println();
		System.out.println("Input: " + input);
		System.out.println("Acceptance: " + success);
		if (success) {
			System.out.println(input + " is valid input");
		} else {
			System.out.println(input + " is invalid input");
		}
		scanner.close();
	}

	private static void computeFirstSets() {
		for (String nonTerminal : grammar.keySet()) {
			computeFirstSet(nonTerminal);
		}
	}

	private static void computeFirstSet(String nonTerminal) {
		if (firstSets.containsKey(nonTerminal)) {
			return; // First set already computed
		}

		Set<String> firstSet = new HashSet<>();

		for (String production : grammar.get(nonTerminal)) {
			String[] symbols = production.split("\\s+");
			String symbol = symbols[0];

			if (isTerminal(symbol)) {
				firstSet.add(symbol);
			} else {
				computeFirstSet(symbol);
				Set<String> firstSetOfSymbol = firstSets.get(symbol);

				firstSet.addAll(firstSetOfSymbol);

				if (firstSetOfSymbol.contains("ε")) {
					int i = 1;

					while (i < symbols.length && firstSetOfSymbol.contains("ε")) {
						symbol = symbols[i];
						computeFirstSet(symbol);
						firstSetOfSymbol = firstSets.get(symbol);

						firstSet.addAll(firstSetOfSymbol);
						i++;
					}

					if (i == symbols.length && firstSetOfSymbol.contains("ε")) {
						firstSet.add("ε");
					}
				}
			}
		}

		firstSets.put(nonTerminal, firstSet);
	}

	private static void computeFollowSets() {
		for (String nonTerminal : grammar.keySet()) {
			computeFollowSet(nonTerminal);
		}

		// Add $ to the follow set of the start symbol
		followSets.get("G").add("$");
	}

	private static void computeFollowSet(String nonTerminal) {
		if (followSets.containsKey(nonTerminal)) {
			return; // Follow set already computed
		}

		Set<String> followSet = new HashSet<>();

		if (nonTerminal.equals("S")) {
			followSet.add("$");
		}

		for (String nt : grammar.keySet()) {
			for (String production : grammar.get(nt)) {
				String[] symbols = production.split("\\s+");

				for (int i = 0; i < symbols.length; i++) {
					String symbol = symbols[i];

					if (symbol.equals(nonTerminal)) {
						if (i == symbols.length - 1) {
							if (!nt.equals(nonTerminal)) {
								computeFollowSet(nt);
								followSet.addAll(followSets.get(nt));
							}
						} else {
							String nextSymbol = symbols[i + 1];

							if (isTerminal(nextSymbol)) {
								followSet.add(nextSymbol);
							} else {
								computeFirstSet(nextSymbol);
								Set<String> firstSetOfNextSymbol = firstSets.get(nextSymbol);

								if (firstSetOfNextSymbol.contains("ε")) {
									followSet.addAll(firstSetOfNextSymbol);
									followSet.remove("ε");

									if (!nt.equals(nonTerminal)) {
										computeFollowSet(nt);
										followSet.addAll(followSets.get(nt));
									}
								} else {
									followSet.addAll(firstSetOfNextSymbol);
								}
							}
						}
					}
				}
			}
		}

		followSets.put(nonTerminal, followSet);
	}

	private static Map<String, Map<String, List<String>>> generateParsingTable() {
		Map<String, Map<String, List<String>>> parsingTable = new HashMap<>();

		for (String nonTerminal : grammar.keySet()) {
			parsingTable.put(nonTerminal, new HashMap<>());
		}

		for (String nonTerminal : grammar.keySet()) {
			List<String> productions = grammar.get(nonTerminal);
			for (String production : productions) {
				Set<String> firstSet = computeProductionFirstSet(production);
				for (String terminal : firstSet) {
					if (!terminal.equals("ε")) {
						List<String> existingProduction = parsingTable.get(nonTerminal).get(terminal);
						if (existingProduction != null) {
							System.out.println("Grammar is not LL(1). Conflict at Non-Terminal: " + nonTerminal
									+ ", Terminal: " + terminal);
							System.exit(0);
						}
						parsingTable.get(nonTerminal).put(terminal, Arrays.asList(production.split("\\s+")));
					}
				}

				if (firstSet.contains("ε")) {
					for (String terminal : followSets.get(nonTerminal)) {
						List<String> existingProduction = parsingTable.get(nonTerminal).get(terminal);
						if (existingProduction != null) {
							System.out.println("Grammar is not LL(1). Conflict at Non-Terminal: " + nonTerminal
									+ ", Terminal: " + terminal);
							System.exit(0);
						}
						parsingTable.get(nonTerminal).put(terminal, Arrays.asList("ε"));
					}
				}
			}
		}

		return parsingTable;
	}

	private static Set<String> computeProductionFirstSet(String production) {
		Set<String> firstSet = new HashSet<>();

		String[] symbols = production.split("\\s+");
		String symbol = symbols[0];

		if (isTerminal(symbol)) {
			firstSet.add(symbol);
		} else {
			firstSet.addAll(firstSets.get(symbol));

			if (firstSets.get(symbol).contains("ε")) {
				int i = 1;

				while (i < symbols.length && firstSets.get(symbol).contains("ε")) {
					symbol = symbols[i];
					firstSet.addAll(firstSets.get(symbol));
					i++;
				}

				if (i == symbols.length && firstSets.get(symbol).contains("ε")) {
					firstSet.add("ε");
				}
			}
		}

		return firstSet;
	}

	private static boolean isTerminal(String symbol) {
		return !grammar.containsKey(symbol);
	}
}
