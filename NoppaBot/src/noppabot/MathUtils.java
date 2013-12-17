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
	
   public static int compare(int x, int y) {
      return (x < y) ? -1 : ((x == y) ? 0 : 1);
  }
}
