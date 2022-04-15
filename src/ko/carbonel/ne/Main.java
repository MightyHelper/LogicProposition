package ko.carbonel.ne;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

		public static Operator valueOf(char c){
			return Arrays.stream(values()).filter(x -> x.name == c).findFirst().orElse(null);
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

	public abstract static class Proposition{
		public String expression;

		protected abstract void parse();
	}

	public static class BaseProposition extends Proposition{

		@Override
		protected void parse() {

		}
	}

	public static class CompoundProposition extends Proposition{
		private Operator mainOperator;
		private Proposition lhs;
		private Proposition rhs;

		public CompoundProposition(String expression) {
			this.expression = expression;

		}

		public void parse() {
			System.out.println("Parse: "+this.expression);
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
			if (startPoint+1!=expression.length()) groupedSubProps.add(expression.substring(startPoint+1));
			List<String> groupedBracketedSubProps = IntStream.range(0, groupedSubProps.size()).mapToObj(x->x%2==0?groupedSubProps.get(x):(Grouper.START.val+groupedSubProps.get(x)+Grouper.END.val)).collect(Collectors.toList());
			List<Operator> ops = Operator.getOperatorsByPrecedence();
			List<String> topLevelLogic = IntStream.range(0, (groupedSubProps.size()+1)/2).map(x->x<<1).mapToObj(groupedSubProps::get).collect(Collectors.toList());
			List<Optional<Operator>> mostImportantOpInGroup = topLevelLogic.stream()
			 .map(x->ops.stream().max(Comparator.comparingInt(operator -> x.indexOf(operator.name)))).collect(Collectors.toList()); // Get highest precedence
			Operator mostImportantOperator = mostImportantOpInGroup.stream()
			 .max(Comparator.comparingInt(x-> x.isEmpty() ? -1000 : x.get().getPrecedence()))
			 .orElse(Optional.empty()).orElse(null);
			if (mostImportantOperator == null){
				return;
//				throw new IllegalArgumentException("Empty Expression? : "+this.expression);
			}
			for (int i = 0; i < topLevelLogic.size(); i++){
				int strt = topLevelLogic.get(i).indexOf(mostImportantOperator.name);
				if (strt !=-1){
					String start = String.join("", groupedBracketedSubProps.subList(0, i)) + topLevelLogic.get(i).substring(0, strt);
					String end = topLevelLogic.get(i).substring(strt+1)+(((i+1)<groupedBracketedSubProps.size())?String.join("", groupedBracketedSubProps.subList(i+1, groupedSubProps.size())):"");
					this.lhs = new CompoundProposition(start);
					this.rhs = new CompoundProposition(end);
					this.mainOperator = mostImportantOperator;
					this.lhs.parse();
					this.rhs.parse();
					return;
				}
			}
		}

		@Override
		public String toString() {
			if (this.mainOperator == null){
				return "["+this.expression+"]";
			}
			return "{"+lhs+mainOperator.name+rhs+"}";
		}
	}

	public static void main(String[] args) {
//		CompoundProposition proposition = new CompoundProposition("a&b|~c|(a>b)|(c>(d|e))&c");
//		CompoundProposition proposition = new CompoundProposition("a&b|c>(a|b)=(a&b)");
		CompoundProposition proposition = new CompoundProposition("(a>b)|(c>(d|e))&c");
		proposition.parse();
		System.out.println(proposition);
	}
}
