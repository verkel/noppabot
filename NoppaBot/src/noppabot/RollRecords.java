package noppabot;
import java.io.*;
import java.util.*;

import com.fasterxml.jackson.databind.*;


/*
 * Created on May 19, 2012
 * @author verkel
 */

public class RollRecords {
	public List<User> users = new ArrayList<User>();
	public String lastWinner;
	public int streak;
	
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
		user.wins++;
		if (user.nick == lastWinner) {
			streak++;
		}
		else {
			streak = 1;
		}
		lastWinner = user.nick;
	}
	
	public void incrementParticipation(String nick) {
		User user = getOrAddUser(nick);
		user.participation++;
	}
	
	public void sortUsers() {
		Collections.sort(users);
	}
	
	public void save(Writer writer) throws IOException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			mapper.writeValue(writer, this);
			
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	public static RollRecords load(Reader reader) throws IOException{
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(reader, RollRecords.class);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
