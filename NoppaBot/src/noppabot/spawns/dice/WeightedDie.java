/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.spawns.*;
import noppabot.spawns.evolved.CrushingDie;


public class WeightedDie extends BasicPowerup {

	public static final int bonus = 10;

	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", getNameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the weighted die falls into emptiness.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s. It weighs just the right amount!", 
			ownerColored, getNameColored());
	}

	@Override
	public int onContestRoll(int roll) {
		int result = roll + bonus;
		String resultStr = resultStr(result);
		result = clamp(result);
		bot.sendChannelFormat(
			"%s's weighted die is so fairly weighted, that the roll goes very smoothly!", owner);
		bot.sendChannelFormat("%s rolls %d + %d = %s! %s", ownerColored, roll, bonus, resultStr,
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
		return new CrushingDie(this, false);
	}
	
	// Upgrade
//	public class CrushingDie extends EvolvedPowerup {
//		private static final int dmgSides = 30;
//		private static final int humongousDmgSides = 10;
//		private static final int humongousDmgBonus = 20;
//		
//		private boolean humongous;
//		
//		public CrushingDie(boolean humongous) {
//			this.humongous = humongous;
//		}
//		
//		@Override
//		public int onContestRoll(int roll) {
//			if (humongous) return HumongousDie.doContestRoll(bot, owner, roll);
//			else {
//				bot.sendChannelFormat("%s drops the crushing die and the ground trembles. %d! %s", ownerColored, roll,
//					bot.grade(roll));
//				return roll;
//			}
//		}
//		
//		@Override
//		public int onOpponentRoll(String opponent, int roll) {
//			if (humongous) return doHumongousCrush(bot, owner, opponent, roll);
//			else return doStandardCrush(bot, owner, opponent, roll);
//		}
//
//		private int doStandardCrush(INoppaBot bot, String owner, String opponent, int roll) {
//			int damage = Powerups.powerupRnd.nextInt(dmgSides) + 1;
//			int result = roll - damage;
//			String resultStr = resultStr(result);
//			result = clamp(result);
//			bot.sendChannelFormat("%s's %s crushes the opposition! %s's roll takes %d damage and drops down to %s.", 
//				owner, getName(), ColorStr.nick(opponent), damage, resultStr);
//			return result;
//		}
//		
//		private int doHumongousCrush(INoppaBot bot, String owner, String opponent, int roll) {
//			int damageRoll = Powerups.powerupRnd.nextInt(humongousDmgSides) + 1;
//			int totalDamage = damageRoll + humongousDmgBonus;
//			int result = roll - totalDamage;
//			String resultStr = resultStr(result);
//			result = clamp(result);
//			bot.sendChannelFormat("%s's %s pulverizes the opposition! %s's roll takes %d + %d = %d damage and drops down to %s.", 
//				owner, getName(), ColorStr.nick(opponent), damageRoll, humongousDmgBonus, totalDamage, resultStr);
//			return result;
//		}
//		
//		@Override
//		public String getName() {
//			return humongous ? "HUMONGOUS CRUSHING DIE" : "Crushing Die";
//		}
//		
//		@Override
//		public String getUpgradeDescription() {
//			if (humongous) return String.format("It now deals d%d + %d " +
//				"crushing damage to others' rolls!", humongousDmgSides, humongousDmgBonus);
//			else return String.format("It loses its bonus, but now deals d%d " +
//				"crushing damage to others' rolls!", dmgSides);
//		}
//	}
}