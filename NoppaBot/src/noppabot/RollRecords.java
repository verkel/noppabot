package noppabot;
import java.util.*;


/*
 * Created on May 19, 2012
 * @author verkel
 */

public class RollRecords {
	public List<User> users = new ArrayList<User>();
	public String lastWinner;
	
	public User getUser(String nick) {
		for (User user : users) {
			if (user.nick.equals(nick)) return user;
		}
		return null;
	}
	
	public User getOrAddUser(String nick) {
		User user = getUser(nick);
		if (user == null) {
			user = new User();
			user.nick = nick;
			users.add(user);
		}
		return user;
	}
	
	public void incrementWins(String nick) {
		User user = getOrAddUser(nick);
		int streak = user.streak;
		clearStreaks();
		user.wins++;
		if (user.nick == lastWinner) {
			user.streak = streak+1;
		}
	}
	
	public void clearStreaks() {
		for (User user : users) {
			user.streak = 0;
		}
	}
}
