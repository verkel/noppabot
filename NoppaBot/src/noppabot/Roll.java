/*
 * Created on 28.7.2014
 * @author verkel
 */
package noppabot;

import java.util.function.IntPredicate;


public interface Roll {
	int total();

	default String totalStr() {
		return String.valueOf(total());
	}
	
	default int intValueClamped() {
		return Roll.clampValue(total(), 100);
	}
	
	default int intValue(boolean clamp) {
		if (clamp) return intValueClamped();
		else return total();
	}
	
	default int intValueRuled(INoppaBot bot) {
		return intValue(bot.getRules().cappedRolls.get());
	}
	
	default String toResultString(INoppaBot bot) {
		return toResultString(true, false, bot);
	}
	
	String toResultString(boolean color, boolean detailed, INoppaBot bot);
	
	default boolean test(IntPredicate predicate) {
		return predicate.test(total());
	}
	
	public static String maybeColorRoll(Roll roll, boolean color, INoppaBot bot) {
		if (color) return colorRoll(roll, bot); // Color it green/red
		else return Color.emphasize(String.valueOf(roll.total())); // Color it hilighted white
	}
	
	public static int clampValue(int value, int sides) {
		return Math.max(0, Math.min(sides, value));
	}
	
	public static String colorRoll(Roll roll, INoppaBot bot) {
		if (bot.getRolls().isWinningRoll(roll)) return Color.winningRoll(roll);
		else return Color.losingRoll(roll);
	}
}
