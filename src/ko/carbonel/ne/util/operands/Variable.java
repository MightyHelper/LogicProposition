package ko.carbonel.ne.util.operands;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Variable extends Operand {
	public static final String TRUE = "1";
	public static final String FALSE = "0";
	public String getName() {
		return name;
	}
	private String name;
	public Variable(String name) {
		this.name = name;
	}
	@Override
	public void prefixVariables(String prefix) {
		this.name = prefix + this.name;
	}
	public boolean calculate(HashMap<String, Boolean> values) {
		if (this.name.equals(TRUE))return true;
		if (this.name.equals(FALSE))return false;
		try{
			return values.get(this.name);
		}catch(NullPointerException ex){
			throw new RuntimeException("Could not find value for proposition: '"+name+"'");
		}
	}
	@Override
	public Operand simplify(boolean toAnd) {
		return this;
	}
	@Override
	public String toString() {
		return name;
	}
	public List<Variable> getVariables(){
		List<Variable> v = new ArrayList<>(1);
		if (!this.name.equals(TRUE) && !this.name.equals(FALSE)) v.add(this);
		return v;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Variable){
			return Objects.equals(this.name, ((Variable) obj).name);
		}
		return super.equals(obj);
	}
}
