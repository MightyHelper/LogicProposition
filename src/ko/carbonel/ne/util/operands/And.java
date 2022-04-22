package ko.carbonel.ne.util.operands;
public class And extends BinaryOperator {
	public static final String repr = "∧";
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
	@Override
	public Operand simplify(boolean toAnd) {
		simplifyArguments(toAnd);
		if (toAnd) return this;
		return new Not(new Or(new Not(this.a), new Not(this.b))).simplify(false);
	}
}
