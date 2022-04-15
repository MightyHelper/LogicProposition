package ko.carbonel.ne;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
	public enum Operator {
		OR('|', 1), AND('&', 1), XOR('^', 2), IMPLIES('>', 3), IMPLIED('<', 3), IFF('=', 4), NOT('-', 0);
		private final char name;
		private final int precedence;

		Operator(char c, int p) {
			this.name = c;
			this.precedence = p;
		}

		public char getName() {
			return name;
		}

		public int getPrecedence() {
			return precedence;
		}

		public static List<Operator> getOperatorsByPrecedence() {
			return Arrays.stream(values()).sorted(Comparator.comparingInt(Operator::getPrecedence)).collect(Collectors.toList());
		}
	}

	public enum Grouper {
		START('('), END(')'), UNKNOWN(' ');
		public final char val;

		Grouper(char c) {
			this.val = c;
		}

		static Grouper valueOf(char c) {
			return Arrays.stream(values()).filter(x -> x.val == c).findFirst().orElse(UNKNOWN);
		}
	}

	public static class Proposition {
		private final String expression;
		private Operator mainOperator;
		private Proposition lhs;
		private Proposition rhs;

		public Proposition(String expression) {
			this.expression = expression;

		}

		public Proposition parse() {
			int openCount = 0;
			int startPoint = -1;
			List<String> groupedSubProps = new ArrayList<>();
			for (int i = 0; i < expression.length(); i++) {
				switch (Grouper.valueOf(expression.charAt(i))) {
					case START -> {
						if (openCount == 0) {
							groupedSubProps.add(expression.substring(startPoint+1, i));
							startPoint = i;
						}
						openCount++;
					}
					case END -> {
						openCount--;
						if (openCount == 0) {
							groupedSubProps.add(expression.substring(startPoint+1, i));
							startPoint = i;
						}
					}
				}
			}
			List<Proposition> subPropositions = groupedSubProps.stream().map(Proposition::new).map(Proposition::parse).collect(Collectors.toList());
			System.out.println(groupedSubProps);
			return this;
		}
	}

	public static void main(String[] args) {
		Proposition proposition = new Proposition("a&b|~c|(a>b)|(c>(d|e))");
		proposition.parse();
	}
}
