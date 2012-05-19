package noppabot;
import java.util.*;


/*
 * Created on May 19, 2012
 * @author verkel
 */

public class User {
	public String nick;
	public int wins;
	public int streak;
	public Set<String> aliases = new TreeSet<String>();
	
	@Override
	public String toString() {
		return nick + ": " + wins;
	}
}
