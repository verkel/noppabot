/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.spawns.*;


public class EnchantedDie extends BasicPowerup {

	public static final int bonus = 15;

	@Override
	public void onSpawn() {
		bot.sendChannelFormat("An %s appears!", getNameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the spell on the enchanted die expires and nobody really wants it anymore.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s and feels a tingling sensation at the fingertips.", 
			ownerColored, getNameColored());
	}

	@Override
	public int onContestRoll() {
		int roll = roll();
		int result = roll + bonus;
		String resultStr = resultStr(result);
		result = clamp(result);
		bot.sendChannelFormat(
			"The enchanted die grants %s either eternal fame and fortune, or a substantial roll bonus. %s chooses the latter.",
			owner, owner);
		bot.sendChannelFormat("%s rolls %d + %d = %s! %s", ownerColored, roll, bonus, resultStr,
			bot.grade(result));
		return result;
	}

	@Override
	public String getName() {
		return "Enchanted Die";
	}
	
	@Override
	public float getSpawnChance() {
		return 0.75f;
	}
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade() {
		return new PotentDie();
	}
	
	// Upgrade
	public class PotentDie extends EvolvedPowerup {
		private static final int bonus = EnchantedDie.bonus + 5;
		
		public PotentDie() {
			super(EnchantedDie.this);
		}
		
		@Override
		public int onContestRoll() {
			int roll = roll();
			int result = roll + bonus;
			String resultStr = resultStr(result);
			result = clamp(result);
			bot.sendChannelFormat("The potent die grants the utmost magical advantage for %s's roll!",
				owner);
			bot.sendChannelFormat("%s rolls %d + %d = %s! %s", owner, roll, bonus, resultStr,
				bot.grade(result));
			return result;
		}
		
		@Override
		public String getName() {
			return "Potent Die";
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("It is now enchanted with the most potent magics, granting it +20 total roll bonus!", bonus);
		}
	}
}