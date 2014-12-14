/*
 * Created on 29.10.2013
 * @author verkel
 */
package noppabot;

import it.sauronsoftware.cron4j.Scheduler;

import java.util.*;

import noppabot.NoppaBot.ExpireTask;
import noppabot.NoppaBot.SpawnTask;
import noppabot.PeekedRoll.Hint;
import noppabot.spawns.*;

public interface INoppaBot {

	public static enum State { NORMAL, ROLL_PERIOD, SETTLE_TIE };
	
//	int clampRoll(int roll);
	void clearFavorsUsed();
	DiceRoll doRoll(String nick, int sides);
	String getDefaultContestRollMessage(String nick, DiceRoll value, boolean colorNick, boolean colorRoll);
	Map<String, Powerup> getPowerups();
	int getPowerupSides(String nick);
	List<String> getRandomPowerupOwners();
	DiceRoll getRoll(String nick, int sides);
	Calendar getRollPeriodStartTime();
	Rolls getRolls();
	Rules getRules();
	int getSecondsAfterPeriodStart();
	Calendar getSpawnEndTime();
	String grade(Roll value);
	void insertApprenticeDice();
	RollRecords loadRollRecords();
	void participate(String nick, Roll rollValue);
	boolean participated(String nick);
	PeekedRoll peekRoll(String nick, int sides);
	String remainingSpawnsInfo();
//	String rollToString(Roll roll);
//	String rollToString(Roll roll, boolean colorRoll);
	ExpireTask scheduleExpire(Powerup powerup, Calendar expireTime);
	SpawnTask scheduleRandomSpawn(Calendar spawnTime, Spawner<BasicPowerup> allowedPowerups, Spawner<Event> allowedEvents);
	SpawnTask scheduleSpawn(Calendar spawnTime, ISpawnable spawn);
	void sendChannel(String msg);
	void sendChannelFormat(String msg, Object... args);
	void sendDefaultContestRollMessage(String nick, DiceRoll roll, boolean colorNick, boolean colorRoll);
	void sendMessage(String nick, String msg);
	void sendMessageFormat(String nick, String msg, Object... args);
	void setNextRoll(String nick, int sides, DiceRoll roll);
	Scheduler getScheduler();
	Object getLock();
	PokerTable getPokerTable();
	State getState();
	Hint spawnRollHint(String nick, int sides);
	PeekableRandom getRandomFor(String nick);
	SortedSet<Powerup> getAvailablePowerups();
	boolean hasFavor(String nick);
}