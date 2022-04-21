package ko.carbonel.ne.util.operands;
public class Not extends UnaryOperator {
	public static String repr = "~";
	public Not(Operand a) {
		super(a);
	}
	@Override
	public boolean compute(boolean va) {
		return !va;
	}
	@Override
	public String toString() {
		return "¬" + a;
	}
}
