/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.INoppaBot;


public class WeightedDie extends Powerup {

	public static final int bonus = 10;

	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("A weighted die appears!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... the weighted die falls into emptiness.");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat("%s grabs the weighted die.", nick);
	}

	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		int result = roll + bonus;
		result = clamp(result);
		bot.sendChannelFormat(
			"%s's weighted die is so fairly weighted, that the roll goes very smoothly!", nick);
		bot.sendChannelFormat("%s rolls %d + %d = %d! %s", nick, roll, bonus, result,
			bot.grade(result));
		return result;
	}

	@Override
	public String getName() {
		return "Weighted Die";
	}
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade() {
		return new CrushingDie();
	}
	
	// Upgrade
	public static class CrushingDie extends Powerup {
		private static final int dmgSides = 30;
		
		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			bot.sendChannelFormat("%s drops the crushing die and the ground trembles. %d! %s", nick, roll,
				bot.grade(roll));
			return roll;
		}
		
		@Override
		public int onOpponentRoll(INoppaBot bot, String owner, String opponent, int roll) {
			int damage = Powerups.powerupRnd.nextInt(dmgSides) + 1;
			int result = roll - damage;
			result = clamp(result);
			bot.sendChannelFormat("%s's die crushes the opposition! %s's roll takes %d damage and drops down to %d.", 
				owner, opponent, damage, result);
			return result;
		}
		
		@Override
		public String getName() {
			return "Crushing Die";
		}
		
		@Override
		public String getUpgradeDescription(INoppaBot bot, String nick) {
			return String.format("It loses its bonus, but now deals d%d crushing damage to others' rolls!", dmgSides);
		}
	}
}