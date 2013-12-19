/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;


public class EnchantedDie extends Powerup {

	public static final int bonus = 15;

	@Override
	public void onSpawn() {
		bot.sendChannel("An enchanted die appears!");
	}

	@Override
	public void onExpire() {
		bot.sendChannelFormat("... the spell on the enchanted die expires and nobody really wants it anymore.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat(
			"%s grabs the enchanted die and feels a tingling sensation at the fingertips.", ownerColored);
	}

	@Override
	public int onContestRoll(int roll) {
		int result = roll + bonus;
		result = clamp(bot, result);
		bot.sendChannelFormat(
			"The enchanted die grants %s either eternal fame and fortune, or a substantial roll bonus. %s chooses the latter.",
			owner, owner);
		bot.sendChannelFormat("%s rolls %d + %d = %d! %s", ownerColored, roll, bonus, result,
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
		public int onContestRoll(int roll) {
			int result = roll + bonus;
			result = clamp(bot, result);
			bot.sendChannelFormat("The potent die grants the utmost magical advantage for %s's roll!",
				owner);
			bot.sendChannelFormat("%s rolls %d + %d = %d! %s", owner, roll, bonus, result,
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