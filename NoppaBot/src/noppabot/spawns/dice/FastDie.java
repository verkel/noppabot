/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.*;

public class FastDie extends Powerup {

	private static final int maxBonus = 20;
	
	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("The fast die appears!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... the fast die was too fast for you.");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat(
			"%s quickly grabs the fast die! It asks you if you can throw it even faster when the time comes.",
			nick);
	}

	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		int seconds = bot.getSecondsAfterPeriodStart();
		int penalty = MathUtils.clamp(seconds - 10, 0, maxBonus);
		int bonus = maxBonus - penalty;
		int result = roll + bonus;
		result = clamp(bot, result);
		bot.sendChannelFormat("%s waited %d seconds before rolling. The fast die awards %d - %d = %d speed bonus!", 
			nick, seconds, maxBonus, penalty, bonus);
		bot.sendChannelFormat("%s rolls %d + %d = %d! %s", nick, roll, bonus, result, bot.grade(result));
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
	public Powerup upgrade(INoppaBot bot) {
		return new FasterDie();
	}
	
	// Upgrade
	public static class FasterDie extends Powerup {
		
		private static final int maxBonus = 30;
		
		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			int seconds = bot.getSecondsAfterPeriodStart();
			int penalty = MathUtils.clamp(seconds, 0, maxBonus);
			int bonus = maxBonus - penalty;
			int result = roll + bonus;
			result = clamp(bot, result);
			bot.sendChannelFormat("%s waited %d seconds before rolling. The faster die awards %d - %d = %d speed bonus!", 
				nick, seconds, maxBonus, penalty, bonus);
			bot.sendChannelFormat("%s rolls %d + %d = %d! %s", nick, roll, bonus, result, bot.grade(result));
			return result;
		}
		
		@Override
		public String getName() {
			return "Faster Die";
		}
		
		@Override
		public String getUpgradeDescription(INoppaBot bot, String nick) {
			return "You now get bonus from the first 10 seconds too.";
		}
	}
}