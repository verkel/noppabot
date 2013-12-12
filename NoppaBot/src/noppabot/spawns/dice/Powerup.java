/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.INoppaBot;
import noppabot.spawns.ISpawnable;

public abstract class Powerup implements ISpawnable {
	
	public abstract String getName();

	public void initialize(INoppaBot bot) {
	}
	
	/**
	 * Say something when the item spawns. Don't initialize the item here, as
	 * it won't be called when a dice pirate generates a new item.
	 */
	public void onSpawn(INoppaBot bot) {
	}

	public void onPickup(INoppaBot bot, String nick) {
	}

	public void onExpire(INoppaBot bot) {
	}

	public void onRollPeriodStart(INoppaBot bot, String nick) {
	}
	
	public void onTiebreakPeriodStart(INoppaBot bot, String nick) {
	}

	public int onNormalRoll(INoppaBot bot, String nick, int roll) {
		return roll;
	}

	/**
	 * Say the appropriate roll message and return the modified roll
	 * 
	 * @return a modified roll
	 */
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		bot.sendDefaultContestRollMessage(nick, roll);
		return roll;
	}
	
	/**
	 * Should the powerup be put into player's item slot
	 */
	public boolean isCarried() {
		return true;
	}
	
	public boolean canPickUp(INoppaBot bot, String nick) {
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
	
//	/**
//	 * Cost or value of the die
//	 */
//	public abstract int getCost();
	
	@Override
	public float getSpawnChance() {
		return 1;
	}

	@Override
	public String toString() {
		return getName();
	}
	
	public int capResult(int roll) {
		return Math.min(100, roll);
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
//	@Override
//	public boolean equals(Object obj) {
//		// This is useful for the upgradeablePowerups set
//		return this.getClass().equals(obj.getClass());
//	}
}