package ko.carbonel.ne.util.operands;
import java.util.HashMap;
import java.util.List;
public abstract class Operand {
	public Operand() {
	}
	abstract public boolean calculate(HashMap<String, Boolean> values);
	abstract public Operand simplify(boolean toAnd);
	abstract public List<Variable> getVariables();
}
