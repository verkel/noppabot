/*
 * Created on 19.12.2013
 * @author verkel
 */
package noppabot.spawns;

import noppabot.*;


public abstract class BasicPowerup extends Powerup {
	
	protected INoppaBot bot;
	protected boolean colorOwner = true;
	protected boolean colorRoll = true;
	protected String owner; // The owner
	protected String ownerColored; // The owner, colored

	@Override
	public final BasicPowerup initialize(INoppaBot bot) {
		this.bot = bot;
		doInitialize();
		return this;
	}

	@Override
	public final void setOwner(String owner) {
		this.owner = owner;
		this.ownerColored = colorOwner ? ColorStr.nick(owner) : owner;
	}
	
	public final void setColors(boolean colorOwner, boolean colorRoll) {
		this.colorOwner = colorOwner;
		this.colorRoll = colorRoll;
	}
	
	@Override
	public String getNameColored() {
		return ColorStr.basicPowerup(getName());
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
	
	public void sendExpireMessageFormat(String msg, Object... args) {
		bot.sendChannelFormat(ColorStr.expires(msg), args);
	}

	/**
	 * Say the appropriate roll message and return the modified roll
	 * 
	 * @return a modified roll
	 */
	@Override
	public int onContestRoll(int roll) {
		bot.sendDefaultContestRollMessage(owner, roll, colorOwner, colorRoll);
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
		return bot.rollToString(roll, colorRoll);
	}
	
	public int clamp(int roll) {
		return bot.clampRoll(roll);
	}
	
	public INoppaBot bot() {
		return bot;
	}
	
	public String owner() {
		return owner;
	}
	
	public String ownerColored() {
		return ownerColored;
	}
	
	public void sendDefaultContestRollMessage(int roll) {
		bot.sendDefaultContestRollMessage(owner, roll, colorOwner, colorRoll);
	}
}
