/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.*;


public class WeightedDie extends Powerup {

	public static final int bonus = 10;

	@Override
	public void onSpawn() {
		bot.sendChannel("A weighted die appears!");
	}

	@Override
	public void onExpire() {
		bot.sendChannelFormat("... the weighted die falls into emptiness.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the weighted die.", ownerColored);
	}

	@Override
	public int onContestRoll(int roll) {
		int result = roll + bonus;
		result = clamp(bot, result);
		bot.sendChannelFormat(
			"%s's weighted die is so fairly weighted, that the roll goes very smoothly!", owner);
		bot.sendChannelFormat("%s rolls %d + %d = %d! %s", ownerColored, roll, bonus, result,
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
		public int onContestRoll(int roll) {
			if (humongous) return HumongousDie.doContestRoll(bot, owner, roll);
			else {
				bot.sendChannelFormat("%s drops the crushing die and the ground trembles. %d! %s", ownerColored, roll,
					bot.grade(roll));
				return roll;
			}
		}
		
		@Override
		public int onOpponentRoll(String opponent, int roll) {
			if (humongous) return doHumongousCrush(bot, owner, opponent, roll);
			else return doStandardCrush(bot, owner, opponent, roll);
		}

		private int doStandardCrush(INoppaBot bot, String owner, String opponent, int roll) {
			int damage = Powerups.powerupRnd.nextInt(dmgSides) + 1;
			int result = roll - damage;
			result = clamp(bot, result);
			bot.sendChannelFormat("%s's %s crushes the opposition! %s's roll takes %d damage and drops down to %d.", 
				owner, getName(), ColorStr.nick(opponent), damage, result);
			return result;
		}
		
		private int doHumongousCrush(INoppaBot bot, String owner, String opponent, int roll) {
			int damageRoll = Powerups.powerupRnd.nextInt(humongousDmgSides) + 1;
			int totalDamage = damageRoll + humongousDmgBonus;
			int result = roll - totalDamage;
			result = clamp(bot, result);
			bot.sendChannelFormat("%s's %s pulverizes the opposition! %s's roll takes %d + %d = %d damage and drops down to %d.", 
				owner, getName(), ColorStr.nick(opponent), damageRoll, humongousDmgBonus, totalDamage, result);
			return result;
		}
		
		@Override
		public String getName() {
			return humongous ? "HUMONGOUS CRUSHING DIE" : "Crushing Die";
		}
		
		@Override
		public String getUpgradeDescription() {
			if (humongous) return String.format("It now deals d%d + %d " +
				"crushing damage to others' rolls!", humongousDmgSides, humongousDmgBonus);
			else return String.format("It loses its bonus, but now deals d%d " +
				"crushing damage to others' rolls!", dmgSides);
		}
	}
}