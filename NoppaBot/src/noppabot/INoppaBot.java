/*
 * Created on 29.10.2013
 * @author verkel
 */
package noppabot;

import java.util.*;

import noppabot.events.Event;
import noppabot.powerups.Powerup;

public interface INoppaBot {

	public Object scheduleSpawn(Calendar spawnTime, Spawner<Powerup> allowedPowerups, Spawner<Event> allowedEvents);
	
	public void sendDefaultContestRollMessage(String nick, int value);

	public String getDefaultContestRollMessage(String nick, int value);

	public String grade(int value);

	public void participate(String nick, int rollValue);
	
	public boolean participated(String nick);

	public RollRecords loadRollRecords();

	public void sendChannelFormat(String msg, Object... args);

	public void sendChannel(String msg);

	public int getRollFor(String nick, int sides);
	
	public int peekRollFor(String nick);
	
	public Map<String, Powerup> getPowerups();
	
	public Map<String, Integer> getRolls();
	
	public int getSecondsAfterMidnight();
	
	public void sendMessage(String nick, String msg);
	
	public void sendMessageFormat(String nick, String msg, Object... args);
	
	public String remainingSpawnsInfo();
	
	public void insertApprenticeDice();

	public List<String> getRandomPowerupOwners();
}