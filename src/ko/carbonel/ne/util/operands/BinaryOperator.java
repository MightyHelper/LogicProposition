package ko.carbonel.ne.util.operands;
import java.util.HashMap;
import java.util.List;
public abstract class BinaryOperator extends Operator {
	protected Operand a;
	protected Operand b;
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
	protected void simplifyArguments(boolean toAnd) {
		this.a = this.a.simplify(toAnd);
		this.b = this.b.simplify(toAnd);
	}
	abstract public boolean compute(boolean va, boolean vb);
	public boolean calculate(HashMap<String, Boolean> values) {
		return compute(a.calculate(values), b.calculate(values));
	}
}
