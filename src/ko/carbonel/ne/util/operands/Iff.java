package ko.carbonel.ne.util.operands;
public class Iff extends BinaryOperator {
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
	@Override
	public Operand simplify(boolean toAnd) {
		this.simplifyArguments(toAnd);
		return new And(new Implies(this.a, this.b), new Implies(this.b, this.a)).simplify(toAnd);
	}
}