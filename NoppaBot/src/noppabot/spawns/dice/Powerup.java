/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.*;
import noppabot.spawns.ISpawnable;

public abstract class Powerup implements ISpawnable {
	
	protected INoppaBot bot;
	protected String owner; // The owner
	protected String ownerColored; // The owner, colored
	
	public abstract String getName();

	public final void initialize(INoppaBot bot) {
		this.bot = bot;
		doInitialize();
	}
	
	public final void setOwner(String owner) {
		this.owner = owner;
		this.ownerColored = ColorStr.nick(owner);
	}
	
	public void doInitialize() {
	}
	
	/**
	 * Say something when the item spawns. Don't initialize the item here, as
	 * it won't be called when a dice pirate generates a new item.
	 */
	public void onSpawn() {
	}

	public void onPickup() {
	}

	public void onExpire() {
	}

	public void onRollPeriodStart() {
	}
	
	public void onTiebreakPeriodStart() {
	}

	public int onNormalRoll(int roll) {
		return roll;
	}
	
	public int onOpponentRoll(String opponent, int roll) {
		return roll;
	}
	
	public void onOpponentRollLate(String opponent, int roll) {
	}
	
	public String getUpgradeDescription() {
		return "";
	}

	/**
	 * Say the appropriate roll message and return the modified roll
	 * 
	 * @return a modified roll
	 */
	public int onContestRoll(int roll) {
		bot.sendDefaultContestRollMessage(owner, roll);
		return roll;
	}
	
	/**
	 * Should the powerup be put into player's item slot
	 */
	public boolean isCarried() {
		return true;
	}
	
	public boolean canPickUp(String nick) {
		if (isCarried() && bot.getPowerups().containsKey(nick)) {
			bot.sendChannelFormat("%s: you already have the %s.", nick, bot.getPowerups().get(nick));
			return false;
		}
		
		return true;
	}
	
	public boolean isUpgradeable() {
		return false;
	}
	
	public Powerup upgrade() {
		return null;
	}
	
	@Override
	public float getSpawnChance() {
		return 1;
	}

	@Override
	public String toString() {
		return getName();
	}
	
	public static String rollToString(INoppaBot bot, int roll) {
		if (roll <= 100 && roll >= 0) {
			return String.valueOf(roll);
		}
		else {
			if (bot.getRules().cappedRolls) {
				return String.format("%d (= %d)", roll, clamp(bot, roll));
			}
			else {
				return String.valueOf(roll);
			}
		}
	}
	
	public static int clamp(INoppaBot bot, int roll) {
		if (bot.getRules().cappedRolls) return Math.max(0, Math.min(100, roll));
		else return roll;
	}
	
	@Override
	public boolean equals(Object obj) {
		// This is useful for removing stuff from the spawning lists
		return this.getClass().equals(obj.getClass());
	}
	
//	@Override
//	public int compareTo(Powerup p) {
//		return cmp(getCost(), p.getCost());
//	}
//	
//	private static int cmp(int c1, int c2) {
//		return c1 > c2 ? 1 : c1 < c2 ? -1 : 0;
//	}
//	

}