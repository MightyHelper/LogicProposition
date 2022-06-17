package ko.carbonel.ne;
import ko.carbonel.ne.util.PresetExpression;
import ko.carbonel.ne.util.Pair;
import ko.carbonel.ne.util.operands.*;
import ko.carbonel.ne.util.Util;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
public class Main {
	public static void main(String[] args) {
		if (args.length < 2) {
			printUsage();
			return;
		}
		String expressionText = switch (args[0]) {
			case "preset" -> PresetExpression.values()[Integer.parseInt(args[1])].value;
			case "custom" -> args[1];
			default -> "";
		};
		if (Objects.equals(args[0], "file")){
			parseFile(args[1]);
		}else{
			String toRepr = args.length == 3 ? args[2] : "none";
			if (expressionText.length() == 0) {
				printUsage();
				return;
			}
			System.out.println("Got input      : " + expressionText);
			String replacedOperations = replacer(expressionText);
			System.out.println("Interpreted as : " + replacedOperations);
			Operand expr = parseExpression(replacedOperations);
			System.out.println("Parsed      as : " + expr);
			expr = switch (toRepr.toLowerCase()){
				case "and" -> expr.simplify(true);
				case "or" -> expr.simplify(false);
				default -> expr;
			};
			System.out.println("Expressed   as : " + expr);
			showOperand(expr);
		}
	}
	private static void parseFile(String path) {
		File f = new File(path);
		try (BufferedReader is = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
			String line = is.readLine();
			System.out.println(line);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static String replacer(String expression) {
		return expression
		 .toLowerCase()
		 .replaceAll("(if )|(it is )", "")
		 .replaceAll(" ?(~)|(-)|(not ?(true ?(that )?)?)|(untrue ?)", Not.repr)
		 .replaceAll("(->)|( ?implies ?)|( ?therefore ?)|( ?then ?)", Implies.repr)
		 .replaceAll("(<->)|( ?iff ?)", Iff.repr)
		 .replaceAll("(&)|( ?and ?)|( ?but ?)", And.repr)
		 .replaceAll("(\\^)|( ?xor ?)", Xor.repr)
		 .replaceAll("(\\|)|( ?or ?)", Or.repr);
	}
	public static void showOperand(Operand op) {
		List<Pair<HashMap<String, Boolean>, Boolean>> truthTable = getTruthTable(op, op.getVariables().stream().map(Variable::getName).distinct().toList());
		System.out.println("Truth Table:");
		System.out.println(prettyPrintTruthTable(truthTable, op.toString()));
	}
	private static void printUsage() {
		System.out.println("Argument usage: [preset <index>|custom \"Proposition\"] (And|Or) ");
		System.out.println("Available preset expressions:");
		Arrays.stream(PresetExpression.values()).map(PresetExpression::toIndexed).forEach(System.out::println);
	}
	public static List<Pair<HashMap<String, Boolean>, Boolean>> getTruthTable(Operand expression, List<String> variables) {
		return IntStream.range(0, 1 << variables.size())
		 .mapToObj(i -> Util.produceVariableValues(i, variables))
		 .map(variableValues -> new Pair<>(variableValues, expression.calculate(variableValues)))
		 .collect(Collectors.toList());
	}
	public static String prettyPrintTruthTable(List<Pair<HashMap<String, Boolean>, Boolean>> truthTable, String title) {
		HashMap<String, Boolean> variables = truthTable.get(0).first();
		if (variables == null) return "";
		List<String> varNames = new ArrayList<>(variables.keySet());
		List<String> headings = Stream.concat(varNames.stream().sorted(), Stream.of(title)).toList();
		List<List<String>> cells = truthTable.stream()
		 .map(key -> Stream.concat(new ArrayList<>(key.first().values()).stream(), Stream.of(key.second())).map(Util::boolToText).toList())
		 .toList();
		StringBuilder output = new StringBuilder();
		List<Integer> columnWidths = headings.stream().map(String::length).map(x -> x + 4).toList();
		IntStream.range(0, columnWidths.size()).mapToObj(i -> Util.padCenter(headings.get(i), columnWidths.get(i))).forEach(output::append);
		output.append("\n");
		cells.forEach(cell -> {
			IntStream.range(0, columnWidths.size()).mapToObj(i -> Util.padCenter(cell.get(i), columnWidths.get(i))).forEach(output::append);
			output.append("\n");
		});
		output.append("Analysis result: ").append(Util.analyseExpressionTruthValue(truthTable));
		return output.toString();
	}
	public static Operand parseExpression(String expression) {
		expression = expression.trim();
		if (expression.startsWith(Not.repr)) return new Not(parseExpression(expression.substring(1)));
		List<String> parts = Util.splitOnFirstLevelBrackets(expression);
		if (!Util.containsAnyOperator(expression)) {
			if (parts.size() == 1 && !parts.get(0).contains(Not.repr)) return new Variable(parts.get(0));
			return parseExpression(parts.get(0));
		}
		List<String> topLevelExpressions = Util.getTopLevelExpressions(parts);
		List<String> subExpressions = Util.getSubExpressions(parts);
		Optional<String> topPrecedenceOperator = Util.getTopPrecedenceOperator(topLevelExpressions);
		if (topPrecedenceOperator.isEmpty()) throw new IllegalArgumentException("No unbracketed expressions allowed:" + expression);
		String topPrecedence = topPrecedenceOperator.get();
		int sectionIndex = topLevelExpressions.indexOf(Util.getExpressionPartThatContains(topLevelExpressions, topPrecedence));
		return finalValueParser(topLevelExpressions, sectionIndex, topPrecedence, subExpressions);
	}
	private static Operand finalValueParser(List<String> topLevelOps, int sectionIndex, String topPrecedence, List<String> subExpressions) {
		String[] mainOperatorDirectArguments = topLevelOps.get(sectionIndex).split(Pattern.quote(topPrecedence), 2);
		List<String> allOperations = Util.mergeLists(topLevelOps, Util.addImpliedBrackets(subExpressions));
		String lhs = String.join("", allOperations.subList(0, sectionIndex << 1)) + mainOperatorDirectArguments[0];
		String rhs = mainOperatorDirectArguments[1] + String.join("", allOperations.subList((sectionIndex << 1) + 1, allOperations.size()));
		return Util.createBinaryOperator(topPrecedence, lhs, rhs);
	}
}
