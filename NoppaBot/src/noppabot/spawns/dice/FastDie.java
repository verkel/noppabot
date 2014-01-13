/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.MathUtils;
import noppabot.spawns.*;

public class FastDie extends BasicPowerup {

	private static final int maxBonus = 20;
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", getNameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the fast die was too fast for you.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat(
			"%s quickly grabs the %s! It asks you if you can throw it even faster when the time comes.",
			ownerColored, getNameColored());
	}

	@Override
	public int onContestRoll() {
		int roll = roll();
		int seconds = bot.getSecondsAfterPeriodStart();
		int penalty = MathUtils.clamp(seconds - 10, 0, maxBonus);
		int bonus = maxBonus - penalty;
		int result = roll + bonus;
		String resultStr = resultStr(result);
		result = clamp(result);
		bot.sendChannelFormat("%s waited %d seconds before rolling. The fast die awards %d - %d = %d speed bonus!", 
			owner, seconds, maxBonus, penalty, bonus);
		bot.sendChannelFormat("%s rolls %d + %d = %s! %s", ownerColored, roll, bonus, resultStr, bot.grade(result));
		return result;
	}

	@Override
	public String getName() {
		return "Fast Die";
	}
	
	
	@Override
	public float getSpawnChance() {
		return 0.50f;
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
	public class FasterDie extends EvolvedPowerup {
		
		private static final int maxBonus = 30;
		
		public FasterDie() {
			super(FastDie.this);
		}
		
		@Override
		public int onContestRoll() {
			int roll = roll();
			int seconds = bot.getSecondsAfterPeriodStart();
			int penalty = MathUtils.clamp(seconds, 0, maxBonus);
			int bonus = maxBonus - penalty;
			int result = roll + bonus;
			String resultStr = resultStr(result);
			result = clamp(result);
			bot.sendChannelFormat("%s waited %d seconds before rolling. The faster die awards %d - %d = %d speed bonus!", 
				owner, seconds, maxBonus, penalty, bonus);
			bot.sendChannelFormat("%s rolls %d + %d = %s! %s", ownerColored, roll, bonus, resultStr, bot.grade(result));
			return result;
		}
		
		@Override
		public String getName() {
			return "Faster Die";
		}
		
		@Override
		public String getUpgradeDescription() {
			return "You now get bonus from the first 10 seconds too.";
		}
	}
}