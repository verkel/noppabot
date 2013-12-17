/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.INoppaBot;


public class LuckyDie extends Powerup {

	public static final int bonus = 25;

	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("A lucky die appears!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... the lucky die rolls away.");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat(
			"%s grabs the lucky die and it wishes good luck for tonight's roll.", nick);
	}

	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		if (String.valueOf(roll).contains("7")) {
			int result = roll + bonus;
			result = clamp(bot, result);
			bot.sendChannelFormat(
				"%s rolls %d! The lucky die likes sevens in numbers, so it tinkers with your roll, making it %d + %d = %d. Lucky!",
				nick, roll, roll, bonus, result);
			return result;
		}
		else {
			bot.sendChannelFormat(
				"%s rolls %d! The lucky die doesn't seem to like this number, though.", nick, roll);
			return roll;
		}
	}

	@Override
	public String getName() {
		return "Lucky Die";
	}
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade(INoppaBot bot) {
		return new JackpotDie();
	}
	
	// Upgrade
	public static class JackpotDie extends Powerup {
		private static final int jackpotBonus = 40;
		
		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			if (String.valueOf(roll).contains("7")) {
				int result = roll + jackpotBonus;
				result = clamp(bot, result);
				bot.sendChannelFormat(
					"%s rolls %d! You win the JACKPOT! Your final roll is %d + %d = %d.",
					nick, roll, roll, jackpotBonus, result);
				return result;
			}
			else {
				bot.sendChannelFormat(
					"%s rolls %d! No jackpot for you.", nick, roll);
				return roll;
			}
		}
		
		@Override
		public String getName() {
			return "Jackpot Die";
		}
		
		@Override
		public String getUpgradeDescription(INoppaBot bot, String nick) {
			return String.format("If your roll contains the digit 7, you now get the jackpot bonus of +%d!", jackpotBonus);
		}
	}
}