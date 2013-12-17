package noppabot;
import java.util.*;


/*
 * Created on May 19, 2012
 * @author verkel
 */

public class User implements Comparable<User> {
	public String nick;
	public int wins;
	public int participation;
	public Set<String> aliases = new TreeSet<String>();
	public LinkedList<Integer> lastRolls = new LinkedList<Integer>();
	
	public void addRoll(int roll) {
		lastRolls.addFirst(roll);
		if (lastRolls.size() > 7) lastRolls.removeLast();
	}
	
	@Override
	public String toString() {
		return nick + ": " + wins;
	}

	@Override
	public int compareTo(User user) {
		return -MathUtils.compare(this.wins, user.wins);
	}

}
