/*
 * Created on 19.12.2013
 * @author verkel
 */
package noppabot;

import org.jibble.pircbot.Colors;

public class ColorStr {

	private ColorStr() {
	}

	private static char ctrlC = '\u0003';//0x3;

	public static String custom(String str, String colorStr) {
		return colorStr + str + Colors.NORMAL;
	}
	
	public static String nick(String nick) {
		return custom(nick, Colors.TEAL);
	}
}
