package ko.carbonel.ne;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
		String delim = "  ";
		List<String> vars = new ArrayList<>(variables.keySet());
		String output = "";
		output += String.join(delim, vars) + delim + title + "\n";
		output += truthTable.keySet().stream().sorted(Comparator.comparingInt(x->
		 x.keySet().stream()
		  .map(k->x.get(k)?vars.size()-vars.indexOf(k)+1:0)
		  .reduce(Integer::sum)
		  .orElse(-1000)
		)).map(key ->
			new ArrayList<>(key.values()).stream().map(Main::boolToText).collect(Collectors.joining(delim))
			 + delim + boolToText(truthTable.get(key)) + "\n"
		 )
		 .collect(Collectors.joining(""));
		return output;
	}
	public static void main(String[] args) {
		Operand expr = new Implies(new Or(new Implies(new Variable("p"), new Variable("q")),new Implies(new Variable("q"), new Variable("r"))), new Implies(new Variable("p"), new Variable("r")));//((p → q) ∧ (q → r)) → (p → r)
		HashMap<HashMap<String, Boolean>, Boolean> truthTable = getTruthTable(expr, Arrays.asList("p", "q", "r"));
		System.out.println(prettyPrintTruthTable(truthTable, expr.toString()));
	}
}
abstract class Operand {
	boolean value;
	public Operand() {
	}
	abstract public boolean calculate(HashMap<String, Boolean> values);
}
class Variable extends Operand {
	private final String name;
	Variable(String name) {
		this.name = name;
	}
	public boolean calculate(HashMap<String, Boolean> values) {
		return values.get(this.name);
	}
	@Override
	public String toString() {
		return name;
	}
}
abstract class Operator extends Operand {
	public Operator() {
	}
}
abstract class BinaryOperator extends Operator {
	Operand a, b;
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
	abstract public boolean compute(boolean va);
	public boolean calculate(HashMap<String, Boolean> values) {
		return compute(a.calculate(values));
	}
}
class Not extends UnaryOperator {
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
	public And(Operand a, Operand b) {
		super(a, b);
	}
	@Override
	public boolean compute(boolean va, boolean vb) {
		return va & vb;
	}
	@Override
	public String toString() {
		return "(" + a + "&" + b + ")";
	}
}
class Or extends BinaryOperator {
	public Or(Operand a, Operand b) {
		super(a, b);
	}
	@Override
	public boolean compute(boolean va, boolean vb) {
		return va | vb;
	}
	@Override
	public String toString() {
		return "(" + a + "|" + b + ")";
	}
}
class Xor extends BinaryOperator {
	public Xor(Operand a, Operand b) {
		super(a, b);
	}
	@Override
	public boolean compute(boolean va, boolean vb) {
		return va ^ vb;
	}
	@Override
	public String toString() {
		return "(" + a + "^" + b + ")";
	}
}
class Implies extends BinaryOperator {
	public Implies(Operand a, Operand b) {
		super(a, b);
	}
	@Override
	public boolean compute(boolean va, boolean vb) {
		return !va | vb;
	}
	@Override
	public String toString() {
		return "(" + a + "->" + b + ")";
	}
}
class Iff extends BinaryOperator {
	public Iff(Operand a, Operand b) {
		super(a, b);
	}
	@Override
	public boolean compute(boolean va, boolean vb) {
		return va == vb;
	}
	@Override
	public String toString() {
		return "(" + a + "<->" + b + ")";
	}
}