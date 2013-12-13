/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.INoppaBot;

public class EnchantedDie extends Powerup {

	public static final int bonus = 15;

	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("An enchanted die appears!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... the spell on the enchanted die expires and nobody really wants it anymore.");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat(
			"%s grabs the enchanted die and feels a tingling sensation at the fingertips.", nick);
	}

	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		int result = roll + bonus;
		result = clamp(result);
		bot.sendChannelFormat(
			"The enchanted die grants %s either eternal fame and fortune, or a substantial roll bonus. %s chooses the latter.",
			nick, nick);
		bot.sendChannelFormat("%s rolls %d + %d = %d! %s", nick, roll, bonus, result,
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
	public static class PotentDie extends Powerup {
		private static final int bonus = EnchantedDie.bonus + 5;
		
		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			int result = roll + bonus;
			result = clamp(result);
			bot.sendChannelFormat("The potent die grants the utmost magical advantage for %s's roll!",
				nick);
			bot.sendChannelFormat("%s rolls %d + %d = %d! %s", nick, roll, bonus, result,
				bot.grade(result));
			return result;
		}
		
		@Override
		public String getName() {
			return "Potent Die";
		}
		
		@Override
		public String getUpgradeDescription(INoppaBot bot, String nick) {
			return String.format("It is now enchanted with the most potent magics, granting it +20 total roll bonus!", bonus);
		}
	}
}