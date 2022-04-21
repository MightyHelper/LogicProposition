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
	abstract public boolean compute(boolean va, boolean vb);
	public boolean calculate(HashMap<String, Boolean> values) {
		return compute(a.calculate(values), b.calculate(values));
	}
}
