package ko.carbonel.ne.util.operands;
public class And extends BinaryOperator {
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
