package ko.carbonel.ne.util;
import ko.carbonel.ne.Main;
import ko.carbonel.ne.util.operands.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
public class Util {
	public static final List<String> PRECEDENCE = Arrays.asList(Or.repr, And.repr, Xor.repr, Implies.repr, Iff.repr);
	private Util(){}
	public static String boolToText(Boolean v) {
		return v ? "T" : "F";
	}
	public static String pad(String s, Integer integer) {
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
	public static List<String> mergeLists(List<String> topLevelOps, List<String> subExpressions) {
		List<String> out = new ArrayList<>();
		for (int i = 0; i < topLevelOps.size() + subExpressions.size(); i++) {
			if ((i & 1) == 0) out.add(topLevelOps.get(i >> 1));
			else out.add(subExpressions.get(i >> 1));
		}
		return out;
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
				if (openCount == 0) {
					groupedSubPropositions.add(expression.substring(startPoint + 1, i));
					startPoint = i;
				}
				openCount++;
			} else if (expression.charAt(i) == ')') {
				openCount--;
				if (openCount == 0) {
					groupedSubPropositions.add(expression.substring(startPoint + 1, i));
					startPoint = i;
				}
			}
		}
		if (startPoint < expression.length() - 1) groupedSubPropositions.add(expression.substring(startPoint + 1));
		return groupedSubPropositions.get(0).equals("") && groupedSubPropositions.size() == 2 ? splitOnFirstLevelBrackets(groupedSubPropositions.get(1)) : groupedSubPropositions;
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
}
