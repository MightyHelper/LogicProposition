package ko.carbonel.ne.util.operands;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Function extends Operator {
	static int instanceCount = 0;
	private final String repr;
	Operand equivalent;
	public Function(String repr, Operand equivalent) {
		instanceCount++;
		this.equivalent = equivalent;
		this.repr = repr;
		equivalent.prefixVariables("Fun%d_".formatted(instanceCount));
	}
	@Override
	public void prefixVariables(String prefix) {
		this.equivalent.prefixVariables(prefix);
	}
	@Override
	public boolean calculate(HashMap<String, Boolean> values) {
		return equivalent.calculate(values);
	}
	@Override
	public Operand simplify(boolean toAnd) {
		this.equivalent = equivalent.simplify(toAnd);
		return this;
	}
	@Override
	public List<Variable> getVariables() {
		return Collections.emptyList();
	}
	public String getRepr() {
		return repr;
	}
}
