package ko.carbonel.ne.util.operands;
public class Implies extends BinaryOperator {
	public static final String repr = "→";
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
	@Override
	public Operand simplify(boolean toAnd) {
		this.simplifyArguments(toAnd);
		return new Or(new Not(this.a), this.b).simplify(toAnd);
	}
}
