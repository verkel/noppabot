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
		result = prime * result + total();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		DiceRoll other = (DiceRoll)obj;
		if (total() != other.total()) return false;
		return true;
	}

	public static final DiceRoll ZERO = new DiceRoll(0);
	
	protected final int value;
	protected final int visibleBonus;

	public DiceRoll(int value) {
		this(value, 0);
	}
	
	public DiceRoll(int value, int visibleBonus) {
		this.value = value;
		this.visibleBonus = visibleBonus;
	}
	
	@Override
	public int total() {
		return value + visibleBonus;
	}
	
	public int baseValue() {
		return value;
	}
	
	public int visibleBonus() {
		return visibleBonus;
	}
	
	public DiceRoll add(int bonus) {
		return new DiceRoll(value + bonus, visibleBonus);
	}
	
	public DiceRoll add(Roll roll) {
		return new DiceRoll(value + roll.total(), visibleBonus);
	}
	
	public DiceRoll addVisibleBonus(int visibleBonus) {
		return new DiceRoll(value, this.visibleBonus + visibleBonus);
	}
	
	public DiceRoll sub(int bonus) {
		return new DiceRoll(value - bonus, visibleBonus);
	}
	
	public DiceRoll sub(Roll roll) {
		return new DiceRoll(value - roll.total(), visibleBonus);
	}
	
	public DiceRoll mul(int factor) {
		return new DiceRoll(value * factor, visibleBonus);
	}
	
	public DiceRoll clamp() {
		return clamp(100);
	}
	
	public DiceRoll clamp(int sides) {
		int clampedValue = Roll.clampValue(value, sides);
		int clampedBonus = Roll.clampValue(visibleBonus, sides - clampedValue);
		return new DiceRoll(clampedValue, clampedBonus);
	}
	
	public String toIntermediateString(INoppaBot bot) {
		if (hasBonus()) return String.format("(%s + %s)", value, getBonusStrColored());
		else return toString();
	}

	private String getBonusStrColored() {
		return Color.visibleRollBonus(visibleBonus);
	}
	
	@Override
	public String toString() {
		return String.valueOf(total());
	}
	
	@Override
	public String toString(boolean color, INoppaBot bot) {
		StringBuilder str = new StringBuilder();
		boolean rollWillBeCapped = willBeCapped(bot);
		boolean stringIsMultipart = rollWillBeCapped || hasBonus();
		if (stringIsMultipart) {
			str.append(value);
			if (hasBonus()) {
				str.append(" + ").append(getBonusStrColored());
			}
			str.append(String.format(" (= %s)",	Roll.maybeColorRoll(this.clamp(), color, bot)));
		}
		else str.append(Roll.maybeColorRoll(this, color, bot));
		return str.toString();
	}

	private boolean hasBonus() {
		return visibleBonus > 0;
	}
	
	private boolean isWithinCap() {
		return total() <= 100 && total() >= 0;
	}
	
	private boolean willBeCapped(INoppaBot bot) {
		return !isWithinCap() && bot.getRules().cappedRolls.get();
	}
	
//	@Override
//	public String toString(boolean color, INoppaBot bot) {
//		int total = total();
//		if (total <= 100 && total >= 0) {
//			return Roll.maybeColorRoll(this, color, bot);
//		}
//		else {
//			if (bot.getRules().cappedRolls.get()) {
//				return String.format("%s (= %s)", total, Roll.maybeColorRoll(this.clamp(), color, bot));
//			}
//			else {
//				return Roll.maybeColorRoll(this, color, bot);
//			}
//		}
//	}
	
//	private void maybeColorRoll(boolean color, INoppaBot bot) {
//		String result = Roll.maybeColorRoll(this, color, bot);
//		if (visibleBonus > 0) result += " + " + Color.instant(visibleBonus);
//	}
}
