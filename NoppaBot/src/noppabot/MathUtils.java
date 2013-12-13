/*
 * Created on 13.12.2013
 * @author verkel
 */
package noppabot;


public class MathUtils {
	private MathUtils() {
	}
	
	public static int clamp(int val, int min, int max) {
	    return Math.max(min, Math.min(max, val));
	}
}
