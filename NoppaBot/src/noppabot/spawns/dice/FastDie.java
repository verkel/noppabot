/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.*;
import noppabot.spawns.*;
import noppabot.spawns.Spawner.SpawnInfo;

public class FastDie extends BasicDie {

	private static final int maxBonus = 20;
	
	public static final BasicPowerupSpawnInfo info = new BasicPowerupSpawnInfo() {

		@Override
		public BasicPowerup create() {
			return new FastDie();
		}
		
		@Override
		public double spawnChance() {
			return 0.50;
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
		sendExpireMessageFormat("... the fast die was too fast for you.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat(
			"%s quickly grabs the %s! It asks you if you can throw it even faster when the time comes.",
			ownerColored, nameColored());
	}

	@Override
	public DiceRoll onContestRoll() {
		DiceRoll roll = roll();
		int seconds = bot.getSecondsAfterPeriodStart();
		int penalty = MathUtils.clamp(seconds - 10, 0, maxBonus);
		int bonus = maxBonus - penalty;
		DiceRoll result = roll.add(bonus);
		bot.sendChannelFormat(
			"%s waited %s seconds before rolling. The fast die awards %s - %s = %s speed bonus!",
			owner, seconds, maxBonus, penalty, bonus);
		bot.sendChannelFormat("%s rolls %s + %s = %s! %s", ownerColored, roll, bonus,
			resultStr(result), bot.grade(result));
		return result;
	}

	@Override
	public String name() {
		return "Fast Die";
	}
	
	@Override
	public int sides() {
		return 100;
	}
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade() {
		return new FasterDie();
	}
	
	// Upgrade
	public class FasterDie extends EvolvedDie {
		
		private static final int maxBonus = 30;
		
		public FasterDie() {
			super(FastDie.this);
		}
		
		@Override
		public DiceRoll onContestRoll() {
			DiceRoll roll = roll();
			int seconds = bot.getSecondsAfterPeriodStart();
			int penalty = MathUtils.clamp(seconds, 0, maxBonus);
			int bonus = maxBonus - penalty;
			DiceRoll result = roll.add(bonus);
			bot.sendChannelFormat(
				"%s waited %s seconds before rolling. The faster die awards %s - %s = %s speed bonus!",
				owner, seconds, maxBonus, penalty, bonus);
			bot.sendChannelFormat("%s rolls %s + %s = %s! %s", ownerColored, roll, bonus,
				resultStr(result), bot.grade(result));
			return result;
		}
		
		@Override
		public String name() {
			return "Faster Die";
		}
		
		@Override
		public String getUpgradeDescription() {
			return "You now get bonus from the first 10 seconds too.";
		}
	}
}