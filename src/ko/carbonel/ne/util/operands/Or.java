package ko.carbonel.ne.util.operands;
public class Or extends BinaryOperator {
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
	@Override
	public Operand simplify(boolean toAnd) {
		this.simplifyArguments(toAnd);
		if (toAnd) return new Not(new And(new Not(this.a), new Not(this.b))).simplify(toAnd);
		return this;
	}
}
