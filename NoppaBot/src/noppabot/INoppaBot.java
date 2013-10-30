/*
 * Created on 29.10.2013
 * @author verkel
 */
package noppabot;

import java.util.Map;

import noppabot.Powerups.Powerup;

public interface INoppaBot {

	public void sendDefaultContestRollMessage(String nick, int value);

	public String getDefaultContestRollMessage(String nick, int value);

	public String grade(int value);

	public void participate(String nick, int rollValue);
	
	public boolean participated(String nick);

	public RollRecords loadRollRecords();

	public void sendChannelFormat(String msg, Object... args);

	public void sendChannel(String msg);

	public int getRollFor(String nick, int sides);
	
	public Map<String, Powerup> getPowerups();
	
	public Map<String, Integer> getRolls();
	
	public int getSecondsAfterMidnight();
	
	public void sendMessage(String nick, String msg);
	
	public void sendMessageFormat(String nick, String msg, Object... args);

}