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
	public final BasicPowerup setOwner(String owner) {
		this.owner = owner;
		this.ownerColored = colorOwner ? Color.nick(owner) : owner;
		setIdentified(true);
		return this;
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
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", nameColored());
	}

	public void sendExpireMessageFormat(String msg, Object... args) {
		bot.sendChannelFormat(Color.expires(msg), args);
	}

	/**
	 * Display the final result. Will not show rolling pro bonus. For cases where
	 * the roll has been mentioned in expanded form earlier (Polished, Weighted
	 * Die, ...)
	 */
	public String resultStr(Roll result) {
		return result.toResultString(shouldColorRoll(), false, bot);
	}

	/**
	 * Display the final result, possible rolling pro bonus expanded. Like:
	 * roll + bonus (= result)
	 * 
	 * Only use if this is the only string shown about the roll. Otherwise rolling pro bonus should
	 * be shown in the left hand side of the roll equation.
	 */
	public String resultStrExp(Roll result) {
		return result.toResultString(shouldColorRoll(), true, bot);
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
		bot.sendDefaultContestRollMessage(owner, roll, colorOwner, shouldColorRoll());
	}
	
	private boolean shouldColorRoll() {
		return colorRoll && bot.rollCanParticipate(owner);
	}
	
	public abstract SpawnInfo<?> spawnInfo();
	
	public boolean isSpawnedBy(SpawnInfo<?> info) {
		return info.equals(spawnInfo());
	}
}
