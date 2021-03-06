/*
 * Created on 19.12.2013
 * @author verkel
 */
package noppabot;

import org.jibble.pircbot.Colors;

import ca.ualberta.cs.poker.Card;

public class Color {

	private Color() {
	}

	public static String custom(String str, String colorStr) {
		return colorStr + str + Colors.NORMAL;
	}
	
	public static String custom(int value, String colorStr) {
		return colorStr + String.valueOf(value) + Colors.NORMAL;
	}
	
	public static String nick(String nick) {
		if (nick.contains("ADVERSARY")) return custom(nick, Colors.RED);
		else return custom(nick, Colors.TEAL);
	}
	
	public static String winningRoll(Roll roll) {
		return custom(roll.totalStr(), Colors.GREEN);
	}
	
	public static String losingRoll(Roll roll) {
		return custom(roll.totalStr(), Colors.RED);
	}
	
	public static String basicPowerup(String powerup) {
		return custom(powerup, Colors.OLIVE);
	}
	
	public static String evolvedPowerup(String powerup) {
		return custom(powerup, Colors.PURPLE);
	}
	
	public static String instant(String instant) {
		return custom(instant, Colors.DARK_GREEN);
	}
	
	public static String visibleRollBonus(int bonus) {
		return visibleRollBonus(String.valueOf(bonus));
	}
	
	public static String visibleRollBonus(String bonus) {
		return custom(bonus, Colors.CYAN);
	}
	
	public static String event(String event) {
		return custom(event, Colors.RED);
	}
	
	public static String expires(String expireMsg) {
		return custom(expireMsg, Colors.NORMAL);
	}
	
	public static String emphasize(int number) {
		return emphasize(String.valueOf(number));
	}

	public static String emphasize(String msg) {
		return custom(msg, Colors.WHITE);
	}
	
	public static String emphasize(Roll roll) {
		return emphasize(String.valueOf(roll.total()));
	}
	
	public static String rulesMode(String name) {
		return custom(name, Colors.MAGENTA);
	}
	
	public static String antiHilight(String nick) {
		return nick.charAt(0) + Colors.BOLD + Colors.BOLD + nick.substring(1);
	}
	
	private static final String BLACK_ON_WHITE = "\u000301,00";
	private static final String RED_ON_WHITE = "\u000305,00";
	
public static String pokerCard(String text, int suit) {
	String color = (suit == Card.HEARTS || suit == Card.DIAMONDS) ? RED_ON_WHITE : BLACK_ON_WHITE;
		return color + text + Colors.NORMAL;
	}
}
