package ko.carbonel.ne.util.operands;
import java.util.HashMap;
import java.util.List;
public abstract class UnaryOperator extends Operator {
	protected Operand a;
	@Override
	public void prefixVariables(String prefix) {
		this.a.prefixVariables(prefix);
	}
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
