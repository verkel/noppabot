package noppabot;
import java.util.*;


/*
 * Created on May 19, 2012
 * @author verkel
 */

public class User implements Comparable<User> {
	public String nick;
	public int wins;
	public int streak;
	public Set<String> aliases = new TreeSet<String>();
	
	@Override
	public String toString() {
		return nick + ": " + wins;
	}

	@Override
	public int compareTo(User user) {
		return -compare(this.wins, user.wins);
	}
	
   public static int compare(int x, int y) {
      return (x < y) ? -1 : ((x == y) ? 0 : 1);
  }
}
