package ko.carbonel.ne.util.operands;
import java.util.HashMap;
import java.util.List;
public abstract class Operand {
	boolean value;
	public Operand() {
	}
	abstract public boolean calculate(HashMap<String, Boolean> values);
	abstract public List<Variable> getVariables();
}
