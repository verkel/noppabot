/*
 * Created on 28.7.2014
 * @author verkel
 */
package noppabot;

/**
 * A dice roll
 * @author verkel
 */
public class DiceRoll implements Roll {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		DiceRoll other = (DiceRoll)obj;
		if (value != other.value) return false;
		return true;
	}

	public static final DiceRoll ZERO = new DiceRoll(0);
	
	protected final int value;

	public DiceRoll(int value) {
		this.value = value;
	}
	
	@Override
	public int intValue() {
		return value;
	}
	
	public int intValueRuled(INoppaBot bot) {
		return intValue(bot.getRules().cappedRolls.get());
	}
	
	public int intValue(boolean clamp) {
		if (clamp) return Roll.clampValue(value);
		else return value;
	}

	public DiceRoll add(int bonus) {
		return new DiceRoll(value + bonus);
	}
	
	public DiceRoll add(Roll roll) {
		return new DiceRoll(value + roll.intValue());
	}
	
	public DiceRoll sub(int bonus) {
		return new DiceRoll(value - bonus);
	}
	
	public DiceRoll sub(Roll roll) {
		return new DiceRoll(value - roll.intValue());
	}
	
	public DiceRoll clamp() {
		return new DiceRoll(Roll.clampValue(value));
	}
	
	@Override
	public String toString() {
		return String.valueOf(value);
	}
	
	@Override
	public String toString(boolean color, INoppaBot bot) {
		if (value <= 100 && value >= 0) {
			return Roll.maybeColorRoll(this, color, bot);
		}
		else {
			if (bot.getRules().cappedRolls.get()) {
				return String.format("%s (= %s)", value, Roll.maybeColorRoll(this.clamp(), color, bot));
			}
			else {
				return Roll.maybeColorRoll(this, color, bot);
			}
		}
	}
}
