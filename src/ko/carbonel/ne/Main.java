package ko.carbonel.ne;
import ko.carbonel.ne.util.Pair;
import ko.carbonel.ne.util.operands.*;
import ko.carbonel.ne.util.operands.Operand;
import ko.carbonel.ne.util.Util;
import ko.carbonel.ne.util.operands.Variable;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
public class Main {
	public static List<Pair<HashMap<String, Boolean>, Boolean>> getTruthTable(Operand expression, List<String> variables) {
		List<Pair<HashMap<String, Boolean>, Boolean>> results = new ArrayList<>();
		for (int i = 0; i < 1 << variables.size(); i++) {
			HashMap<String, Boolean> variableValues = produceVariableValues(i, variables);
			results.add(new Pair<>(variableValues, expression.calculate(variableValues)));
		}
		return results;
	}
	private static HashMap<String, Boolean> produceVariableValues(int i, List<String> variables) {
		HashMap<String, Boolean> outMap = new HashMap<>();
		IntStream.range(0, variables.size()).forEach(j -> outMap.put(variables.get(j), (i & (1 << j)) != 0));
		return outMap;
	}
	public static String prettyPrintTruthTable(List<Pair<HashMap<String, Boolean>, Boolean>> truthTable, String title) {
		HashMap<String, Boolean> variables = truthTable.get(0).first;
		if (variables == null) return "";
		List<String> varNames = new ArrayList<>(variables.keySet());
		List<String> headings = Stream.concat(varNames.stream().sorted(), Stream.of(title)).toList();
		List<List<String>> cells = truthTable.stream()
		 .map(key -> Stream.concat(new ArrayList<>(key.first.values()).stream(), Stream.of(key.second)).map(Util::boolToText).toList())
		 .toList();
		StringBuilder output = new StringBuilder();
		List<Integer> columnWidths = headings.stream().map(String::length).map(x -> x + 4).toList();
		IntStream.range(0, columnWidths.size()).mapToObj(i -> Util.pad(headings.get(i), columnWidths.get(i))).forEach(output::append);
		output.append("\n");
		cells.forEach(cell -> {
			IntStream.range(0, columnWidths.size()).mapToObj(i -> Util.pad(cell.get(i), columnWidths.get(i))).forEach(output::append);
			output.append("\n");
		});
		if (truthTable.stream().allMatch(x -> x.second)) output.append("Tautology");
		else if (truthTable.stream().noneMatch(x -> x.second)) output.append("Contradiction");
		else output.append("Contingency");
		return output.toString();
	}
	public static Operand parseExpression(String expression) {
		expression = expression.trim();
		if (expression.startsWith(Not.repr)) return new Not(parseExpression(expression.substring(1)));
		List<String> parts = Util.splitOnFirstLevelBrackets(expression);
		if (!containsOperator(expression)) return parts.size() == 1 ? new Variable(expression) : parseExpression(parts.get(0));
		List<String> topLevelExpressions = IntStream.range(0, (parts.size() + 1) >> 1).map(x -> x << 1).mapToObj(parts::get).toList();
		List<String> subExpressions = IntStream.range(0, parts.size() >> 1).map(x -> (x << 1) + 1).mapToObj(parts::get).toList();
		Optional<String> topPrecedenceOperator = Util.getTopPrecedenceOperator(topLevelExpressions);
		if (topPrecedenceOperator.isEmpty()) throw new IllegalArgumentException("No unbracketed expressions:" + expression);
		String topPrecedence = topPrecedenceOperator.get();
		int sectionIndex = topLevelExpressions.indexOf(getExpressionPartThatContains(topLevelExpressions, topPrecedence));
		return finalValueParser(topLevelExpressions, sectionIndex, topPrecedence, subExpressions);
	}
	private static String getExpressionPartThatContains(List<String> topLevelExpressions, String topPrecedence) {
		// We already checked this
		//noinspection OptionalGetWithoutIsPresent
		return topLevelExpressions.stream().filter(x -> x.contains(topPrecedence)).findFirst().get();
	}
	private static boolean containsOperator(String expression) {
		return Arrays.stream(expression.split("")).anyMatch(Util.PRECEDENCE::contains);
	}
	private static Operand finalValueParser(List<String> topLevelOps, int sectionIndex, String topPrecedence, List<String> subExpressions) {
		String[] mainOperatorDirectArguments = topLevelOps.get(sectionIndex).split(topPrecedence, 2);
		List<String> allOperations = Util.mergeLists(topLevelOps, addImpliedBrackets(subExpressions));
		String lhs = String.join("", allOperations.subList(0, sectionIndex << 1)) + mainOperatorDirectArguments[0];
		String rhs = mainOperatorDirectArguments[1] + String.join("", allOperations.subList((sectionIndex << 1) + 1, allOperations.size()));
		return Util.createBinaryOperator(topPrecedence, lhs, rhs);
	}
	private static List<String> addImpliedBrackets(List<String> elements) {
		return elements.stream().map("(%s)"::formatted).toList();
	}
	public static String replacer(String expression) {
		return expression
		 .toLowerCase()
		 .replaceAll("(if )|(it is )", "")
		 .replaceAll(" ?(~)|(-)|(not ?(true ?(that )?)?)|(untrue ?)", Not.repr)
		 .replaceAll("(->)|( ?implies ?)|( ?therefore ?)|( ?then ?)", Implies.repr)
		 .replaceAll("(<->)|( ?iff ?)", Iff.repr)
		 .replaceAll("(&)|( ?and ?)|( ?but ?)", And.repr)
		 .replaceAll("(\\|)|( ?or ?)", Or.repr)
		 .replaceAll("(\\^)|( ?xor ?)", Xor.repr);
	}
	public static void main(String[] args) {
		String EX_13_1 = "((p → q) ∧ (q → r)) → (p → r)";
		String EX_13_2 = "((p ∨ q) ∧ (p → r) ∧ (q → r)) → r";
		String EX_13_3 = "[p → (q → r)] → [(p → q) → (p → r)]";
		String EX_18 = "t and (not s) and (p or (q and not r))";
		String EX_19_Example = "If a person has a headache and a person feels tired then the person has a cold";
		String EX_19_1 = "((if my cat has strong legs then my cat can jump onto the counter) and (my cat is on the counter and my cat is tall then my cat can reach the treats) but (my cat has strong legs and not my cat is tall)) therefore not my cat can reach the treats";
		String EX_20_1 = "(1 then a1) and (1 then a2) and (a1 and a2 and a3 then a4) and (a1 and a2 and a4 then a5) and (a1 and a2 and a3 and a4 then a6) and (a5 and a6 then a7) and (a2 then a3) and (a7 then 0)";
		String EX_20_2 = "(1 then a1) and (1 then a2) and (a1 and a2 and a4 then a3) and (a1 and a5 and a6) and (a2 and a7 then a5) and (a1 and a3 and a5 then a7) and (a2 then a4) and (a4 then a8) and (a2 and a3 and a4 then a9) and(a3 and a9 then a6) and (a6 and a7 then a8) and (a7 and a8 and a9 then 0)";
		if (args.length <= 0) {
			System.out.println("Argument usage: [preset <index>|custom \"Proposition\"] ");
			System.out.println("Available expressions:");
			Arrays.asList(
			 "0:EX_13_1       : " + EX_13_1,
			 "1:EX_13_2       : " + EX_13_2,
			 "2:EX_13_3       : " + EX_13_3,
			 "3:EX_18         : " + EX_18,
			 "4:EX_19_Example : " + EX_19_Example,
			 "5:EX_19_1       : " + EX_19_1,
			 "6:EX_20_1       : " + EX_20_1,
			 "7:EX_20_2       : " + EX_20_2
			).forEach(System.out::println);
			return;
		}
		String expressionText = switch (args[0]) {
			case "preset" ->
			 Arrays.asList(EX_13_1, EX_13_2, EX_13_3, EX_18, EX_19_Example, EX_19_1, EX_20_1, EX_20_2).get(Integer.parseInt(args[1]));
			default -> args[1];
		};
		Operand expr = parseExpression(replacer(expressionText));
		List<Pair<HashMap<String, Boolean>, Boolean>> truthTable = getTruthTable(expr, expr.getVariables().stream().map(Variable::getName).distinct().toList());
		System.out.println(prettyPrintTruthTable(truthTable, expr.toString()));
	}
}
