/*
 * Created on 19.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.*;


public abstract class BasicPowerup extends Powerup {
	
	protected INoppaBot bot;
	protected String owner; // The owner
	protected String ownerColored; // The owner, colored

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

	/**
	 * Say the appropriate roll message and return the modified roll
	 * 
	 * @return a modified roll
	 */
	@Override
	public int onContestRoll(int roll) {
		bot.sendDefaultContestRollMessage(owner, roll);
		return roll;
	}
	
	public boolean canPickUp(String nick) {
		if (isCarried() && bot.getPowerups().containsKey(nick)) {
			bot.sendChannelFormat("%s: you already have the %s.", nick, bot.getPowerups().get(nick));
			return false;
		}
		
		return true;
	}
	
	public String resultStr(int roll) {
		return bot.rollToString(roll);
	}
	
	public int clamp(int roll) {
		return bot.clampRoll(roll);
	}
	
}
