package ko.carbonel.ne.util.operands;
public class Implies extends BinaryOperator {
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
