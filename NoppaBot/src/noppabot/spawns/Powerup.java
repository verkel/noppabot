/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns;

import noppabot.*;

public abstract class Powerup<R extends Roll> implements ISpawnable, IColorStrConvertable {
	
	private boolean identified = false;
	
	public boolean isIdentified() {
		return identified;
	}
	
	public void setIdentified(boolean identified) {
		this.identified = identified;
	}

	@Override
	public abstract String name();
	
	public abstract String nameColored();
	
	public String details() {
		return "";
	}
	
	@Override
	public String nameWithDetails() {
		return nameWithDetails(identified, name(), details());
	}
	
	public String nameWithDetailsColored() {
		return nameWithDetails(identified, nameColored(), details());
	}
	
	public static String nameWithDetails(boolean identified, String name, String details) {
		if (identified && !details.isEmpty()) return name + ": " + details;
		else return name;
	}
	
	public abstract Powerup initialize(INoppaBot bot);
	
	public abstract void setOwner(String owner);

	public void onRollPeriodStart() {
	}
	
	public void onTiebreakPeriodStart() {
	}

	public boolean canPickUp(String nick, boolean verbose) {
		INoppaBot bot = bot();
		if (isCarried() && bot.getPowerups().containsKey(nick)) {
			Powerup powerup = bot.getPowerups().get(nick);
			if (verbose) bot.sendChannelFormat("%s: you already have the %s.", 
				Color.nick(nick), powerup.nameWithDetailsColored());
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
	public abstract R onContestRoll();
	
	public Roll onOpponentRoll(String opponent, Roll roll) {
		return roll;
	}
	
	public void onOpponentRollLate(String opponent, Roll roll) {
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
	
	/**
	 * If the powerup is not carried, this property dictates
	 * whether it is destroyed after being picked up
	 */
	public boolean isDestroyedAfterPickup() {
		return false;
	}
	
	public boolean isUpgradeable() {
		return false;
	}
	
	public Powerup upgrade() {
		return this;
	}
	
//	@Override
//	public float spawnChance() {
//		return 1;
//	}
	
	/**
	 * Get the number of sides the die used in this powerup has. If the powerup
	 * rolls multiple dice, return the number of sides in the largest die. The
	 * Diceteller will use this value to predict a roll of a suitable die, if
	 * a powerup is equipped.
	 */
	public abstract int sides();

	@Override
	public String toString() {
		return nameWithDetails();
	}
	
	@Override
	public String toStringColored() {
		return nameWithDetailsColored();
	}
	
//	public abstract R roll(int sides);
//	
//	/**
//	 * Roll d100 and return the result
//	 */
//	public R roll() {
//		return roll(100);
//	}
	
	public abstract INoppaBot bot();
	
	public abstract String owner();
	
	public abstract String ownerColored();

//	@Override
//	public boolean equals(Object obj) {
//		// This is useful for removing stuff from the spawning lists
//		return this.getClass().equals(obj.getClass());
//	}
}