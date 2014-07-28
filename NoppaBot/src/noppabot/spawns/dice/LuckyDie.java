/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.DiceRoll;
import noppabot.spawns.*;
import noppabot.spawns.Spawner.SpawnInfo;

public class LuckyDie extends BasicDie {

	public static final int bonus = 25;

	public static final BasicPowerupSpawnInfo info = new BasicPowerupSpawnInfo() {

		@Override
		public BasicPowerup create() {
			return new LuckyDie();
		}
		
	};
	
	@Override
	public SpawnInfo<?> spawnInfo() {
		return info;
	}
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", nameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the lucky die rolls away.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s and it wishes good luck for tonight's roll.",
			ownerColored, nameColored());
	}

	@Override
	public DiceRoll onContestRoll() {
		DiceRoll roll = roll();
		if (containsSevens(roll)) {
			DiceRoll result = roll.add(bonus);
			bot.sendChannelFormat(
				"%s rolls %d! The lucky die likes sevens in numbers, so it tinkers with your roll, making it %d + %d = %s. Lucky!",
				ownerColored, roll, roll, bonus, resultStr(result));
			return result;
		}
		else {
			bot.sendChannelFormat(
				"%s rolls %s! The lucky die doesn't seem to like this number, though.", ownerColored, resultStr(roll));
			return roll;
		}
	}

	public static boolean containsSevens(DiceRoll roll) {
		return String.valueOf(roll).contains("7");
	}

	@Override
	public String name() {
		return "Lucky Die";
	}
	
	@Override
	public int sides() {
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
	public class JackpotDie extends EvolvedDie {
		private static final int jackpotBonus = 40;
		
		public JackpotDie() {
			super(LuckyDie.this);
		}
		
		@Override
		public DiceRoll onContestRoll() {
			DiceRoll roll = roll();
			if (containsSevens(roll)) {
				DiceRoll result = roll.add(jackpotBonus);
				bot.sendChannelFormat(
					"%s rolls %d! You win the JACKPOT! Your final roll is %d + %d = %s.",
					ownerColored, roll, roll, jackpotBonus, resultStr(result));
				return result;
			}
			else {
				bot.sendChannelFormat(
					"%s rolls %d! No jackpot for you.", ownerColored, roll);
				return roll;
			}
		}
		
		@Override
		public String name() {
			return "Jackpot Die";
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("If your roll contains the digit 7, you now get the jackpot bonus of +%d!", jackpotBonus);
		}
	}
}