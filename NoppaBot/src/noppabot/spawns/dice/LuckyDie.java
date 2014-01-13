/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.spawns.*;



public class LuckyDie extends BasicPowerup {

	public static final int bonus = 25;

	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", getNameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the lucky die rolls away.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s and it wishes good luck for tonight's roll.",
			ownerColored, getNameColored());
	}

	@Override
	public int onContestRoll() {
		int roll = roll();
		if (String.valueOf(roll).contains("7")) {
			int result = roll + bonus;
			String resultStr = resultStr(result);
			result = clamp(result);
			bot.sendChannelFormat(
				"%s rolls %d! The lucky die likes sevens in numbers, so it tinkers with your roll, making it %d + %d = %s. Lucky!",
				ownerColored, roll, roll, bonus, resultStr);
			return result;
		}
		else {
			bot.sendChannelFormat(
				"%s rolls %s! The lucky die doesn't seem to like this number, though.", ownerColored, resultStr(roll));
			return roll;
		}
	}

	@Override
	public String getName() {
		return "Lucky Die";
	}
	
	@Override
	public int getSides() {
		return 100;
	}
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade() {
		return new JackpotDie();
	}
	
	// Upgrade
	public class JackpotDie extends EvolvedPowerup {
		private static final int jackpotBonus = 40;
		
		public JackpotDie() {
			super(LuckyDie.this);
		}
		
		@Override
		public int onContestRoll() {
			int roll = roll();
			if (String.valueOf(roll).contains("7")) {
				int result = roll + jackpotBonus;
				String resultStr = resultStr(result);
				result = clamp(result);
				bot.sendChannelFormat(
					"%s rolls %d! You win the JACKPOT! Your final roll is %d + %d = %s.",
					ownerColored, roll, roll, jackpotBonus, resultStr);
				return result;
			}
			else {
				bot.sendChannelFormat(
					"%s rolls %d! No jackpot for you.", ownerColored, roll);
				return roll;
			}
		}
		
		@Override
		public String getName() {
			return "Jackpot Die";
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("If your roll contains the digit 7, you now get the jackpot bonus of +%d!", jackpotBonus);
		}
	}
}