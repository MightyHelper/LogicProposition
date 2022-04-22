package ko.carbonel.ne.util;
import ko.carbonel.ne.Main;
import ko.carbonel.ne.util.operands.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.IntStream;
public class Util {
	public static final List<String> PRECEDENCE = Arrays.asList(Or.repr, And.repr, Xor.repr, Implies.repr, Iff.repr);
	private Util() {
	}
	public static String boolToText(Boolean v) {
		return v ? "T" : "F";
	}
	public static String padCenter(String s, Integer integer) {
		int start = (integer - s.length()) >> 1;
		int end = integer - s.length() - start;
		return " ".repeat(Math.max(start, 0)) + s + " ".repeat(Math.max(end, 0));
	}
	public static HashMap<String, Class<?>> getBinaryOperatorMapper() {
		HashMap<String, Class<?>> binaryOperatorMapper = new HashMap<>();
		binaryOperatorMapper.put(Iff.repr, Iff.class);
		binaryOperatorMapper.put(Implies.repr, Implies.class);
		binaryOperatorMapper.put(Xor.repr, Xor.class);
		binaryOperatorMapper.put(And.repr, And.class);
		binaryOperatorMapper.put(Or.repr, Or.class);
		return binaryOperatorMapper;
	}
	public static List<String> mergeLists(List<String> topLevelExpressions, List<String> subExpressions) {
		return IntStream.range(0, topLevelExpressions.size() + subExpressions.size())
		 .mapToObj(i -> ((i & 1) == 0 ? topLevelExpressions : subExpressions).get(i >> 1))
		 .toList();
	}
	public static List<String> splitOnFirstLevelBrackets(String expression) {
		int openCount = 0;
		int startPoint = -1;
		expression = expression
		 .replaceAll("\\[", "(")
		 .replaceAll("]", ")");
		List<String> groupedSubPropositions = new ArrayList<>();
		for (int i = 0; i < expression.length(); i++) {
			if (expression.charAt(i) == '(') {
				startPoint = resetStartAndAddSubProposition(expression, openCount, startPoint, groupedSubPropositions, i);
				openCount++;
			} else if (expression.charAt(i) == ')') {
				openCount--;
				startPoint = resetStartAndAddSubProposition(expression, openCount, startPoint, groupedSubPropositions, i);
			}
		}
		if (startPoint < expression.length() - 1) groupedSubPropositions.add(expression.substring(startPoint + 1));
		return groupedSubPropositions.get(0).length() == 0 && groupedSubPropositions.size() == 2 ?
		 splitOnFirstLevelBrackets(groupedSubPropositions.get(1)) :
		 groupedSubPropositions;
	}
	private static int resetStartAndAddSubProposition(String expression, int openCount, int startPoint, List<String> groupedSubPropositions, int i) {
		if (openCount != 0) return startPoint;
		groupedSubPropositions.add(expression.substring(startPoint + 1, i));
		return i;
	}
	public static Operand createBinaryOperator(String operator, String lhs, String rhs) {
		HashMap<String, Class<?>> binaryMapper = getBinaryOperatorMapper();
		try {
			return (Operand) (binaryMapper.get(operator)).getConstructor(Operand.class, Operand.class).newInstance(Main.parseExpression(lhs), Main.parseExpression(rhs));
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			System.out.println("Error creating dynamic binary operator");
			e.printStackTrace();
		}
		return null;
	}
	public static Optional<String> getTopPrecedenceOperator(List<String> expressions) {
		return PRECEDENCE.stream().max(Comparator.comparingInt(x -> expressions.stream().anyMatch(k -> k.contains(x)) ? PRECEDENCE.indexOf(x) : -1));
	}
	public static List<String> getSubExpressions(List<String> parts) {
		return IntStream.range(0, parts.size() >> 1).map(x -> (x << 1) + 1).mapToObj(parts::get).toList();
	}
	public static List<String> getTopLevelExpressions(List<String> parts) {
		return IntStream.range(0, (parts.size() + 1) >> 1).map(x -> x << 1).mapToObj(parts::get).toList();
	}
	public static boolean containsAnyOperator(String expression) {
		return Arrays.stream(expression.split("")).anyMatch(PRECEDENCE::contains);
	}
	public static HashMap<String, Boolean> produceVariableValues(int i, List<String> variables) {
		HashMap<String, Boolean> outMap = new HashMap<>();
		IntStream.range(0, variables.size()).forEach(j -> outMap.put(variables.get(j), (i & (1 << j)) != 0));
		return outMap;
	}
	public static String analyseExpressionTruthValue(List<Pair<HashMap<String, Boolean>, Boolean>> truthTable) {
		if (truthTable.stream().allMatch(x -> x.second)) return "Tautology";
		else if (truthTable.stream().noneMatch(x -> x.second)) return "Contradiction";
		else return "Contingency";
	}
	public static List<String> addImpliedBrackets(List<String> elements) {
		return elements.stream().map("(%s)"::formatted).toList();
	}
	public static String getExpressionPartThatContains(List<String> expressions, String operator) {
		return expressions.stream()
		 .filter(x -> x.contains(operator))
		 .findFirst()
		 .orElseThrow(() -> new RuntimeException("Tried to locate previously located operator and failed."));
	}
}
