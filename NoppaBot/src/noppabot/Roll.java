/*
 * Created on 28.7.2014
 * @author verkel
 */
package noppabot;

import java.util.function.IntPredicate;


public interface Roll {
	int intValue();
	
	default int intValueClamped() {
		return Roll.clampValue(intValue());
	}
	
	default int intValue(boolean clamp) {
		if (clamp) return intValueClamped();
		else return intValue();
	}
	
	default int intValueRuled(INoppaBot bot) {
		return intValue(bot.getRules().cappedRolls.get());
	}
	
	default String toString(INoppaBot bot) {
		return toString(true, bot);
	}
	
	String toString(boolean color, INoppaBot bot);
	
	default boolean test(IntPredicate predicate) {
		return predicate.test(intValue());
	}
	
	public static String maybeColorRoll(Roll roll, boolean color, INoppaBot bot) {
		if (color) return colorRoll(roll, bot); // Color it green/red
		else return Color.emphasize(String.valueOf(roll.intValue())); // Color it hilighted white
	}
	
	public static int clampValue(int value) {
		return Math.max(0, Math.min(100, value));
	}
	
	public static String colorRoll(Roll roll, INoppaBot bot) {
		if (bot.getRolls().isWinningRoll(roll)) return Color.winningRoll(roll);
		else return Color.losingRoll(roll);
	}
}
