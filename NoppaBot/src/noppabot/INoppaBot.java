/*
 * Created on 29.10.2013
 * @author verkel
 */
package noppabot;

import java.util.*;

import noppabot.NoppaBot.ExpireTask;
import noppabot.NoppaBot.SpawnTask;
import noppabot.spawns.*;
import ca.ualberta.cs.poker.Hand;

public interface INoppaBot {

	int clampRoll(int roll);
	void clearFavorsUsed();
	int doRoll(String nick, int sides);
	String getDefaultContestRollMessage(String nick, int value, boolean colorNick, boolean colorRoll);
	public Hand getPokerTableCards();
	Map<String, Powerup> getPowerups();
	int getPowerupSides(String nick);
	List<String> getRandomPowerupOwners();
	int getRoll(String nick, int sides);
	Calendar getRollPeriodStartTime();
	Rolls getRolls();
	Rules getRules();
	int getSecondsAfterPeriodStart();
	Calendar getSpawnEndTime();
	String grade(int value);
	void insertApprenticeDice();
	RollRecords loadRollRecords();
	void onRulesChanged();
	void participate(String nick, int rollValue);
	boolean participated(String nick);
	int peekRoll(String nick, int sides, boolean makeItKnown);
	String remainingSpawnsInfo();
	String rollToString(int roll);
	String rollToString(int roll, boolean colorRoll);
	ExpireTask scheduleExpire(Powerup powerup, Calendar expireTime);
	SpawnTask scheduleRandomSpawn(Calendar spawnTime, Spawner<BasicPowerup> allowedPowerups, Spawner<Event> allowedEvents);
	SpawnTask scheduleSpawn(Calendar spawnTime, ISpawnable spawn);
	void sendChannel(String msg);
	void sendChannelFormat(String msg, Object... args);
	void sendDefaultContestRollMessage(String nick, int value, boolean colorNick, boolean colorRoll);
	void sendMessage(String nick, String msg);
	void sendMessageFormat(String nick, String msg, Object... args);
	void setNextRoll(String nick, int sides, int roll);
}