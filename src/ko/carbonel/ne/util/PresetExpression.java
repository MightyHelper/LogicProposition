package ko.carbonel.ne.util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public enum PresetExpression {
	EX_13_1("((p → q) ∧ (q → r)) → (p → r)"),
	EX_13_2("((p ∨ q) ∧ (p → r) ∧ (q → r)) → r"),
	EX_13_3("[p → (q → r)] → [(p → q) → (p → r)]"),
	EX_18("t and (not s) and (p or (q and not r))"),
	EX_19_EXAMPLE("If a person has a headache and a person feels tired then the person has a cold"),
	EX_19_1("((if my cat has strong legs then my cat can jump onto the counter) and (my cat is on the counter and my cat is tall then my cat can reach the treats) but (my cat has strong legs and not my cat is tall)) therefore not my cat can reach the treats"),
	EX_20_1("(1 then a1) and (1 then a2) and (a1 and a2 and a3 then a4) and (a1 and a2 and a4 then a5) and (a1 and a2 and a3 and a4 then a6) and (a5 and a6 then a7) and (a2 then a3) and (a7 then 0)"),
	EX_20_2("(1 then a1) and (1 then a2) and (a1 and a2 and a4 then a3) and (a1 and a5 and a6) and (a2 and a7 then a5) and (a1 and a3 and a5 then a7) and (a2 then a4) and (a4 then a8) and (a2 and a3 and a4 then a9) and(a3 and a9 then a6) and (a6 and a7 then a8) and (a7 and a8 and a9 then 0)");
	public final String value;
	PresetExpression(String value) {
		this.value = value;
	}
	public String toIndexed() {
		int index = List.of(PresetExpression.values()).indexOf(this);
		return "%d:%s - %s".formatted(index, this, this.value);
	}
}
