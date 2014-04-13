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
		onInitialize();
		return this;
	}
	
	@Override
	public final void setOwner(String owner) {
		this.owner = owner;
		this.ownerColored = colorOwner ? Color.nick(owner) : owner;
		setIdentified(true);
	}
	
	public final void setColors(boolean colorOwner, boolean colorRoll) {
		this.colorOwner = colorOwner;
		this.colorRoll = colorRoll;
	}
	
	@Override
	public String nameColored() {
		return Color.basicPowerup(name());
	}
	
	public void onInitialize() {
	}
	
	/**
	 * Say something when the item spawns. Don't initialize the item here, as
	 * it won't be called when a dice pirate generates a new item.
	 */
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", nameColored());
	}

	public void sendExpireMessageFormat(String msg, Object... args) {
		bot.sendChannelFormat(Color.expires(msg), args);
	}

	/**
	 * Say the appropriate roll message and return the modified roll
	 * 
	 * @return a modified roll
	 */
	@Override
	public int onContestRoll() {
		return bot.doRoll(owner, 100);
	}
	
	public String resultStr(int roll) {
		return bot.rollToString(roll, colorRoll);
	}
	
	public int clamp(int roll) {
		return bot.clampRoll(roll);
	}
	
	@Override
	public INoppaBot bot() {
		return bot;
	}
	
	@Override
	public String owner() {
		return owner;
	}
	
	@Override
	public String ownerColored() {
		return ownerColored;
	}
	
	@Override
	public int roll(int sides) {
		return bot.getRoll(owner, sides);
	}
	
	public void sendDefaultContestRollMessage(int roll) {
		bot.sendDefaultContestRollMessage(owner, roll, colorOwner, colorRoll);
	}
}
