/*
 * Created on 19.12.2013
 * @author verkel
 */
package noppabot;

import org.jibble.pircbot.Colors;

public class ColorStr {

	private ColorStr() {
	}

	public static String custom(String str, String colorStr) {
		return colorStr + str + Colors.NORMAL;
	}
	
	public static String custom(int value, String colorStr) {
		return colorStr + String.valueOf(value) + Colors.NORMAL;
	}
	
	public static String nick(String nick) {
		return custom(nick, Colors.TEAL);
	}
	
	public static String winningRoll(int roll) {
		return custom(String.valueOf(roll), Colors.GREEN);
	}
	
	public static String losingRoll(int roll) {
		return custom(String.valueOf(roll), Colors.RED);
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
	
	public static String event(String event) {
		return custom(event, Colors.RED);
	}
	
	public static String expires(String expireMsg) {
		return custom(expireMsg, Colors.NORMAL);
	}
	
	public static String hilight(int number) {
		return hilight(String.valueOf(number));
	}

	public static String hilight(String msg) {
		return custom(msg, Colors.WHITE);
	}
}
