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

	public RollRecords loadRollRecords();

	public void sendChannelFormat(String msg, Object... args);

	public void sendChannel(String msg);

	public int getRollFor(String nick, int sides);
	
	public Map<String, Powerup> getPowerups();
	
	public int getSecondsAfterMidnight();

}