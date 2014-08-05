/*
 * Created on 19.12.2013
 * @author verkel
 */
package noppabot.spawns;

import noppabot.*;


public abstract class BasicPowerup<R extends Roll> extends Powerup<R> {
	
	protected INoppaBot bot;
	protected boolean colorOwner = true;
	protected boolean colorRoll = true;
	protected String owner; // The owner
	protected String ownerColored; // The owner, colored

	protected static final BasicPowerupSpawnInfo dontSpawnInfo = new BasicPowerupSpawnInfo() {

		@Override
		public boolean spawnInAllPowerups() {
			return false;
		}
		
		@Override
		public BasicPowerup create() {
			return null;
		}
	};
	
	public static abstract class BasicPowerupSpawnInfo implements SpawnInfo<BasicPowerup> {
		
		public boolean spawnInAllPowerups() {
			return true;
		}
		
		public boolean spawnInFirstPowerups() {
			return true;
		}
		
		public boolean spawnInDiceStormPowerups() {
			return true;
		}
		
		public boolean spawnInDiceBrosPowerups() {
			return true;
		}
	}

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

	public String resultStr(Roll result) {
		return result.toString(colorRoll, bot);
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
	
	public void sendDefaultContestRollMessage(DiceRoll roll) {
		bot.sendDefaultContestRollMessage(owner, roll, colorOwner, colorRoll);
	}
	
	public abstract SpawnInfo<?> spawnInfo();
	
	public boolean isSpawnedBy(SpawnInfo<?> info) {
		return info.equals(spawnInfo());
	}
}
