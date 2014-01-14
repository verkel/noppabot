/*
 * Created on 29.10.2013
 * @author verkel
 */
package noppabot;

import java.util.*;

import noppabot.NoppaBot.ExpireTask;
import noppabot.NoppaBot.SpawnTask;
import noppabot.spawns.*;

public interface INoppaBot {

	public SpawnTask scheduleSpawn(Calendar spawnTime, ISpawnable spawn);
	
	public SpawnTask scheduleRandomSpawn(Calendar spawnTime, Spawner<BasicPowerup> allowedPowerups, Spawner<Event> allowedEvents);
	
	public void sendDefaultContestRollMessage(String nick, int value, boolean colorNick, boolean colorRoll);

	public String getDefaultContestRollMessage(String nick, int value, boolean colorNick, boolean colorRoll);

	public String grade(int value);

	public void participate(String nick, int rollValue);
	
	public boolean participated(String nick);

	public RollRecords loadRollRecords();

	public void sendChannelFormat(String msg, Object... args);

	public void sendChannel(String msg);

	public int getRoll(String nick, int sides);
	
	public int peekRoll(String nick, int sides);
	
	public Map<String, Powerup> getPowerups();
	
	public Rolls getRolls();
	
	public int getSecondsAfterPeriodStart();
	
	public void sendMessage(String nick, String msg);
	
	public void sendMessageFormat(String nick, String msg, Object... args);
	
	public String remainingSpawnsInfo();
	
	public void insertApprenticeDice();

	public List<String> getRandomPowerupOwners();
	
	public Calendar getRollPeriodStartTime();
	
	public Calendar getSpawnEndTime();
	
	public Rules getRules();

	public void onRulesChanged();

	int clampRoll(int roll);
	
	String rollToString(int roll);

	String rollToString(int roll, boolean colorRoll);
	
	public int doNormalRoll(String nick, int sides);

	ExpireTask scheduleExpire(Powerup powerup, Calendar expireTime);
}