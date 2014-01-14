/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns;

import noppabot.*;

public abstract class Powerup implements ISpawnable, IColorStrConvertable {
	
	public abstract String name();
	
	public abstract String nameColored();
	
	public abstract Powerup initialize(INoppaBot bot);
	
	public abstract void setOwner(String owner);

	public void onRollPeriodStart() {
	}
	
	public void onTiebreakPeriodStart() {
	}

	public abstract int onNormalRoll();
	
	public boolean canPickUp(String nick) {
		INoppaBot bot = bot();
		if (isCarried() && bot.getPowerups().containsKey(nick)) {
			bot.sendChannelFormat("%s: you already have the %s.", nick, bot.getPowerups().get(nick));
			return false;
		}
		
		return true;
	}
	
	public void onExpire() {
		// Generic expire message. 
		// Evolved powerups can also expire if someone throws them to the ground.
		bot().sendChannelFormat("... the %s expires.", name());
	}
	
	public void onPickup() {
		// Generic pickup message. 
		// Evolved powerups can also be picked up if someone throws them to the ground.
		bot().sendChannelFormat("%s grabs the %s.", ownerColored(), nameColored());
	}


	/**
	 * Say the appropriate roll message and return the final roll
	 */
	public abstract int onContestRoll();
	
	public int onOpponentRoll(String opponent, int roll) {
		return roll;
	}
	
	public void onOpponentRollLate(String opponent, int roll) {
	}
	
	public String getUpgradeDescription() {
		return "";
	}

	/**
	 * Should the powerup be put into player's item slot
	 */
	public boolean isCarried() {
		return true;
	}
	
	public boolean isUpgradeable() {
		return false;
	}
	
	public Powerup upgrade() {
		return null;
	}
	
	@Override
	public float spawnChance() {
		return 1;
	}
	
	/**
	 * Get the number of sides the die used in this powerup has. If the powerup
	 * rolls multiple dice, return the number of sides in the largest die. The
	 * Diceteller will use this value to predict a roll of a suitable die, if
	 * a powerup is equipped.
	 */
	public abstract int sides();

	@Override
	public String toString() {
		return name();
	}
	
	@Override
	public String toStringColored() {
		return nameColored();
	}
	
	public abstract int roll(int sides);
	
	/**
	 * Roll d100 and return the result
	 */
	public int roll() {
		return roll(100);
	}
	
	public abstract INoppaBot bot();
	
	public abstract String owner();
	
	public abstract String ownerColored();

	@Override
	public boolean equals(Object obj) {
		// This is useful for removing stuff from the spawning lists
		return this.getClass().equals(obj.getClass());
	}
}