package ko.carbonel.ne;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
public class Main {
	public static HashMap<HashMap<String, Boolean>, Boolean> getTruthTable(Operand expression, List<String> variables) {
		HashMap<HashMap<String, Boolean>, Boolean> results = new HashMap<>();
		for (int i = 0; i < 1 << variables.size(); i++) {
			HashMap<String, Boolean> variableValues = produceVariableValues(i, variables);
			results.put(variableValues, expression.calculate(variableValues));
		}
		return results;
	}
	private static HashMap<String, Boolean> produceVariableValues(int i, List<String> variables) {
		HashMap<String, Boolean> outMap = new HashMap<>();
		IntStream.range(0, variables.size()).forEach(j -> outMap.put(variables.get(j), (i & (1 << j)) != 0));
		return outMap;
	}
	public static String boolToText(Boolean v) {
		return v ? "T" : "F";
	}
	public static String prettyPrintTruthTable(HashMap<HashMap<String, Boolean>, Boolean> truthTable, String title) {
		HashMap<String, Boolean> variables = truthTable.keySet().stream().findAny().orElse(null);
		if (variables == null) return "";
		List<String> varNames = new ArrayList<>(variables.keySet());
		List<String> headings = Stream.concat(varNames.stream(), Stream.of(title)).toList();
		List<List<String>> cells = truthTable.keySet().stream().sorted(Comparator.comparingInt(x ->
		 x.keySet().stream()
			.map(k -> x.get(k) ? varNames.size() - varNames.indexOf(k) : 0)
			.reduce(Integer::sum)
			.orElse(-1000)
		)).map(key -> Stream.concat(new ArrayList<>(key.values()).stream(), Stream.of(truthTable.get(key)))
		 .map(Main::boolToText).toList()
		).toList();
		StringBuilder output = new StringBuilder();
		List<Integer> columnWidths = headings.stream().map(String::length).map(x->x+4).toList();
		IntStream.range(0, columnWidths.size()).mapToObj(i -> pad(headings.get(i), columnWidths.get(i))).forEach(output::append);
		output.append("\n");
		cells.forEach(cell->{
			IntStream.range(0, columnWidths.size()).mapToObj(i -> pad(cell.get(i), columnWidths.get(i))).forEach(output::append);
			output.append("\n");
		});
		if (truthTable.values().stream().allMatch(x -> x)) output.append("Tautology");
		else if (truthTable.values().stream().noneMatch(x -> x)) output.append("Contradiction");
		else output.append("Contingency");
		return output.toString();
	}
	private static String pad(String s, Integer integer) {
		int start = (integer - s.length())>>1;
		int end = integer - s.length() - start;
		return " ".repeat(Math.max(start, 0)) + s + " ".repeat(Math.max(end, 0));
	}
	public static List<String> getTree(String expression) {
		int openCount = 0;
		int startPoint = -1;
		expression = expression
		 .replaceAll("\\[", "(")
		 .replaceAll("]", ")");
		List<String> groupedSubProps = new ArrayList<>();
		for (int i = 0; i < expression.length(); i++) {
			if (expression.charAt(i) == '(') {
				if (openCount == 0) {
					groupedSubProps.add(expression.substring(startPoint + 1, i));
					startPoint = i;
				}
				openCount++;
			} else if (expression.charAt(i) == ')') {
				openCount--;
				if (openCount == 0) {
					groupedSubProps.add(expression.substring(startPoint + 1, i));
					startPoint = i;
				}
			}
		}
		if (startPoint < expression.length() - 1) groupedSubProps.add(expression.substring(startPoint + 1));
		return groupedSubProps.get(0).equals("") && groupedSubProps.size() == 2 ? getTree(groupedSubProps.get(1)) : groupedSubProps;
	}
	public static Operand parseExpression(String expression) {
		expression = expression.trim();
		if (expression.startsWith(Not.repr)){
			return new Not(parseExpression(expression.substring(1)));
		}
		List<String> parts = getTree(expression);
		List<String> precedence = Arrays.asList(Or.repr, And.repr, Xor.repr, Implies.repr, Iff.repr);
		if (Arrays.stream(expression.split("")).noneMatch(precedence::contains)) {
			return !parts.get(0).equals(expression) ? parseExpression(parts.get(0)) : new Variable(expression);
		}
		List<String> topLevelOps = IntStream.range(0, (parts.size() + 1) >> 1).map(x -> x << 1).mapToObj(parts::get).toList();
		List<String> subExpressions = IntStream.range(0, parts.size() >> 1).map(x -> (x << 1) + 1).mapToObj(parts::get).toList();
		String topAbrev = String.join("", topLevelOps);
		String topPrecedence = precedence.stream().max(Comparator.comparingInt(x -> topAbrev.contains(x) ? precedence.indexOf(x) : -1)).orElse("=");
		if (!topAbrev.contains(topPrecedence)) {
			throw new IllegalArgumentException("No unbracketed expressions:" +expression);
		}
		int sectionIndex = topLevelOps.indexOf(topLevelOps.stream().filter(x -> x.contains(topPrecedence)).findFirst().orElse(null));
		return finalValueParser(topLevelOps, sectionIndex, topPrecedence, subExpressions);
	}
	private static Operand finalValueParser(List<String> topLevelOps, int sectionIndex, String topPrecedence, List<String> subExpressions) {
		HashMap<String, Class<?>> binaryMapper = new HashMap<>();
		binaryMapper.put(Iff.repr, Iff.class);
		binaryMapper.put(Implies.repr, Implies.class);
		binaryMapper.put(Xor.repr, Xor.class);
		binaryMapper.put(And.repr, And.class);
		binaryMapper.put(Or.repr, Or.class);
		String[] parts = topLevelOps.get(sectionIndex).split(topPrecedence, 2);
		List<String> fullOperations = merge(topLevelOps, subExpressions.stream().map(x -> "(" + x + ")").toList());
		String lhs = String.join("", fullOperations.subList(0, sectionIndex << 1)) + parts[0];
		String rhs = parts[1] + String.join("", fullOperations.subList((sectionIndex << 1) + 1, fullOperations.size()));
		try {
			return (Operand) (binaryMapper.get(topPrecedence)).getConstructor(Operand.class, Operand.class).newInstance(parseExpression(lhs), parseExpression(rhs));
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}
	private static List<String> merge(List<String> topLevelOps, List<String> subExpressions) {
		List<String> out = new ArrayList<>();
		for (int i = 0; i < topLevelOps.size() + subExpressions.size(); i++) {
			if ((i & 1) == 0) out.add(topLevelOps.get(i >> 1));
			else out.add(subExpressions.get(i >> 1));
		}
		return out;
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
		if (args.length <= 0){
			System.out.println("Usage:");
			System.out.println("Available expressions:");
			Arrays.asList(
			 "0:EX_13_1       : "+EX_13_1,
			 "1:EX_13_2       : "+EX_13_2,
			 "2:EX_13_3       : "+EX_13_3,
			 "3:EX_18         : "+EX_18,
			 "4:EX_19_Example : "+EX_19_Example,
			 "5:EX_19_1       : "+EX_19_1,
			 "6:EX_20_1       : "+EX_20_1,
			 "7:EX_20_2       : "+EX_20_2
			).forEach(System.out::println);
			return;
		}
		Operand expr;
		String expressionText = switch (args[0]) {
			case "preset" -> Arrays.asList(EX_13_1, EX_13_2, EX_13_3, EX_18, EX_19_Example, EX_19_1, EX_20_1, EX_20_2).get(Integer.parseInt(args[1]));
			default -> args[1];
		};
		expr = parseExpression(replacer(expressionText));
		HashMap<HashMap<String, Boolean>, Boolean> truthTable = getTruthTable(expr, expr.getVariables().stream().map(Variable::getName).distinct().toList());
		System.out.println(prettyPrintTruthTable(truthTable, expr.toString()));
	}
}
abstract class Operand {
	boolean value;
	public Operand() {
	}
	abstract public boolean calculate(HashMap<String, Boolean> values);
	abstract public List<Variable> getVariables();
}
class Variable extends Operand {
	public static String TRUE = "1";
	public static String FALSE = "0";
	public String getName() {
		return name;
	}
	private final String name;
	Variable(String name) {
		this.name = name;
	}
	public boolean calculate(HashMap<String, Boolean> values) {
		if (this.name.equals(TRUE))return true;
		if (this.name.equals(FALSE))return false;
		try{
			return values.get(this.name);
		}catch(NullPointerException ex){
			throw new RuntimeException("Could not find value for proposition: '"+name+"'");
		}
	}
	@Override
	public String toString() {
		return name;
	}
	public List<Variable> getVariables(){
		List<Variable> v = new ArrayList<>(1);
		if (!this.name.equals(TRUE) && !this.name.equals(FALSE)) v.add(this);
		return v;
	}
}
abstract class Operator extends Operand {
	public Operator() {
	}
}
abstract class BinaryOperator extends Operator {
	Operand a, b;
	@Override
	public List<Variable> getVariables() {
		List<Variable> out = this.a.getVariables();
		out.addAll(this.b.getVariables());
		return out;
	}
	public BinaryOperator(Operand a, Operand b) {
		this.a = a;
		this.b = b;
	}
	abstract public boolean compute(boolean va, boolean vb);
	public boolean calculate(HashMap<String, Boolean> values) {
		return compute(a.calculate(values), b.calculate(values));
	}
}
abstract class UnaryOperator extends Operator {
	Operand a;
	public UnaryOperator(Operand a) {
		this.a = a;
	}
	@Override
	public List<Variable> getVariables() {
		return this.a.getVariables();
	}
	abstract public boolean compute(boolean va);
	public boolean calculate(HashMap<String, Boolean> values) {
		return compute(a.calculate(values));
	}
}
class Not extends UnaryOperator {
	public static String repr = "~";
	public Not(Operand a) {
		super(a);
	}
	@Override
	public boolean compute(boolean va) {
		return !va;
	}
	@Override
	public String toString() {
		return "¬" + a;
	}
}
class And extends BinaryOperator {
	public static String repr = "∧";
	public And(Operand a, Operand b) {
		super(a, b);
	}
	@Override
	public boolean compute(boolean va, boolean vb) {
		return va & vb;
	}
	@Override
	public String toString() {
		return "(" + a + repr + b + ")";
	}
}
class Or extends BinaryOperator {
	public static String repr = "∨";
	public Or(Operand a, Operand b) {
		super(a, b);
	}
	@Override
	public boolean compute(boolean va, boolean vb) {
		return va | vb;
	}
	@Override
	public String toString() {
		return "(" + a + repr + b + ")";
	}
}
class Xor extends BinaryOperator {
	public static String repr = "^";
	public Xor(Operand a, Operand b) {
		super(a, b);
	}
	@Override
	public boolean compute(boolean va, boolean vb) {
		return va ^ vb;
	}
	@Override
	public String toString() {
		return "(" + a + repr + b + ")";
	}
}
class Implies extends BinaryOperator {
	public static String repr = "→";
	public Implies(Operand a, Operand b) {
		super(a, b);
	}
	@Override
	public boolean compute(boolean va, boolean vb) {
		return !va | vb;
	}
	@Override
	public String toString() {
		return "(" + a + repr + b + ")";
	}
}
class Iff extends BinaryOperator {
	public static String repr = "=";
	public Iff(Operand a, Operand b) {
		super(a, b);
	}
	@Override
	public boolean compute(boolean va, boolean vb) {
		return va == vb;
	}
	@Override
	public String toString() {
		return "(" + a + repr + b + ")";
	}
}