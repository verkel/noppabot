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
		result = clamp(bot, result);
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
	public Powerup upgrade(INoppaBot bot) {
		return new CrushingDie(false);
	}
	
	// Upgrade
	public static class CrushingDie extends Powerup {
		private static final int dmgSides = 30;
		private static final int humongousDmgSides = 10;
		private static final int humongousDmgBonus = 20;
		
		private boolean humongous;
		
		public CrushingDie(boolean humongous) {
			this.humongous = humongous;
		}
		
		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			if (humongous) return HumongousDie.doContestRoll(bot, nick, roll);
			else {
				bot.sendChannelFormat("%s drops the crushing die and the ground trembles. %d! %s", nick, roll,
					bot.grade(roll));
				return roll;
			}
		}
		
		@Override
		public int onOpponentRoll(INoppaBot bot, String owner, String opponent, int roll) {
			if (humongous) return doHumongousCrush(bot, owner, opponent, roll);
			else return doStandardCrush(bot, owner, opponent, roll);
		}

		private int doStandardCrush(INoppaBot bot, String owner, String opponent, int roll) {
			int damage = Powerups.powerupRnd.nextInt(dmgSides) + 1;
			int result = roll - damage;
			result = clamp(bot, result);
			bot.sendChannelFormat("%s's %s crushes the opposition! %s's roll takes %d damage and drops down to %d.", 
				owner, getName(), opponent, damage, result);
			return result;
		}
		
		private int doHumongousCrush(INoppaBot bot, String owner, String opponent, int roll) {
			int damageRoll = Powerups.powerupRnd.nextInt(humongousDmgSides) + 1;
			int totalDamage = damageRoll + humongousDmgBonus;
			int result = roll - totalDamage;
			result = clamp(bot, result);
			bot.sendChannelFormat("%s's %s pulverizes the opposition! %s's roll takes %d + %d = %d damage and drops down to %d.", 
				owner, getName(), opponent, damageRoll, humongousDmgBonus, totalDamage, result);
			return result;
		}
		
		@Override
		public String getName() {
			return humongous ? "HUMONGOUS CRUSHING DIE" : "Crushing Die";
		}
		
		@Override
		public String getUpgradeDescription(INoppaBot bot, String nick) {
			if (humongous) return String.format("It now deals d%d + %d " +
				"crushing damage to others' rolls!", humongousDmgSides, humongousDmgBonus);
			else return String.format("It loses its bonus, but now deals d%d " +
				"crushing damage to others' rolls!", dmgSides);
		}
	}
}