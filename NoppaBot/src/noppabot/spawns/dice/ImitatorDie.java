/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.*;
import noppabot.spawns.*;

public class ImitatorDie extends BasicDie {

	private DiceRoll lastRoll = null;
	
	public static final BasicPowerupSpawnInfo info = new BasicPowerupSpawnInfo() {

		@Override
		public BasicPowerup create() {
			return new ImitatorDie();
		}
		
		@Override
		public double spawnChance() {
			return 0.33;
		}
		
		@Override
		public boolean spawnInDiceBrosPowerups() {
			return false;
		}
	};
	
	@Override
	public SpawnInfo<?> spawnInfo() {
		return info;
	}
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", nameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the imitator die found no audience.");
	}

	@Override
	public boolean canPickUp(String nick, boolean verbose) {
		if (super.canPickUp(nick, verbose)) {
			DiceRoll lastRoll = getYesterdaysRoll(nick);
			if (lastRoll == null || lastRoll.equals(DiceRoll.ZERO)) {
				if (verbose) bot.sendChannelFormat("%s: cannot do that, you have no roll to imitate.", Color.nick(nick));
				return false;
			}
			else {
				this.lastRoll = lastRoll;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s and teaches how to roll %s to it.", 
			owner, nameColored(), Color.emphasize(lastRoll.intValue()));
	}
	
	@Override
	public DiceRoll onContestRoll() {
		// Owner might've changed, re-retrieve the roll
		lastRoll = getYesterdaysRoll(owner);
		
		if (lastRoll != null && lastRoll.intValue() > 0) {
			DiceRoll penalty = roll(10);
			String mimicGrade = getMimicGrade(penalty);
			DiceRoll result = lastRoll.sub(penalty);
			bot.sendChannelFormat("%s's %s mimics the yesterday's roll%s", owner, name(), mimicGrade);
			bot.sendChannelFormat("%s rolls %s - %s = %s! %s", 
				ownerColored, lastRoll, penalty, resultStr(result), bot.grade(result));
			return result;
		}
		else {
			bot.sendChannel("The imitator die has nothing to mimic.");
			return super.onContestRoll(); // Normal behaviour
		}
	}
	
	private String getMimicGrade(DiceRoll penalty) {
		int p = penalty.intValue();
		if (p == 1) return " very skillfully!";
		if (p <= 3) return " with great confidence.";
		if (p <= 5) return " with good results.";
		if (p <= 7) return ", but makes some mistakes.";
		if (p <= 9) return ", albeit with great difficulty.";
		return ", but you barely recognize it as the same roll.";
	}

	private DiceRoll getYesterdaysRoll(String nick) {
		Integer lastRoll = null;
		RollRecords records = bot.loadRollRecords();
		if (records != null) {
			lastRoll = records.getOrAddUser(nick).lastRolls.peekFirst();
		}
		return lastRoll == null ? null : new DiceRoll(lastRoll);
	}

	@Override
	public String name() {
		return "Imitator Die";
	}
	
	@Override
	public int sides() {
		return 10;
	}
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade() {
		return new GroundhogDie();
	}
	
	// Upgrade
	public class GroundhogDie extends EvolvedDie {
		
		private static final int bonus = 10;
		
		public GroundhogDie() {
			super(ImitatorDie.this);
		}
		
		@Override
		public DiceRoll onContestRoll() {
			lastRoll = getYesterdaysRoll(owner);
			
			if (lastRoll != null && lastRoll.intValue() > 0) {
				DiceRoll result = lastRoll;
				bot.sendChannelFormat("%s throws the groundhog die with a familiar motion.", owner);
				sendDefaultContestRollMessage(result);
				return result;
			}
			else {
				bot.sendChannel("The groundhog die fails to repeat yesterday's events.");
				return super.onContestRoll(); // Normal behaviour
			}
		}
		
		@Override
		public String name() {
			return "Groundhog Die";
		}
		
		@Override
		public int sides() {
			return 100;
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("It will now roll the exact same number you rolled yesterday.");
		}
	}
}