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
	
	public static String nick(String nick) {
		return custom(nick, Colors.TEAL);
	}
	
	public static String winningRoll(int roll) {
		return custom(String.valueOf(roll), Colors.GREEN);
	}
	
	public static String losingRoll(int roll) {
		return custom(String.valueOf(roll), Colors.RED);
	}
}
