/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.DiceRoll;
import noppabot.spawns.*;
import noppabot.spawns.Spawner.SpawnInfo;


public class UniformDie extends BasicDie {

	public static final BasicPowerupSpawnInfo info = new BasicPowerupSpawnInfo() {

		@Override
		public BasicPowerup create() {
			return new UniformDie();
		}
	};
	
	@Override
	public SpawnInfo<?> spawnInfo() {
		return info;
	}
	
	public static final int bonus = 15;

	@Override
	public void onSpawn() {
		bot.sendChannelFormat("An %s appears!", nameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the uniform die serves another day.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s recruits the %s and it promises to serve you well.", 
			ownerColored, nameColored());
	}

	@Override
	public DiceRoll onContestRoll() {
		DiceRoll roll = roll();
		char[] chars = roll.toString().toCharArray();
		
		if (chars.length == 2) {
			int digit = Character.getNumericValue(chars[0]);
			int digit2 = Character.getNumericValue(chars[1]);
			int distance = Math.abs(digit - digit2);
			int multiplier = 10 - distance;
			DiceRoll result = new DiceRoll(multiplier * 10);
			
			bot.sendChannelFormat(
				"%s rolls %s with the uniform die. You get %s roll multiplier from the distance of your roll's digits.",
				owner, roll, multiplier);
			bot.sendChannelFormat("%s's roll result is set to %s * %s = %s!", ownerColored, multiplier, 10,
				resultStr(result));
			return result;
		}
		else {
			bot.sendChannelFormat(
				"%s rolls %s with the uniform die. The die doesn't know how to assess the uniformity of this roll.",
				ownerColored, resultStr(roll));
			return roll;
		}
	}

	@Override
	public String name() {
		return "Uniform Die";
	}
	
	@Override
	public int sides() {
		return 100;
	}
	
	@Override
	public boolean isUpgradeable() {
		return false;
	}
	
//	@Override
//	public Powerup upgrade() {
//		return new PotentDie();
//	}
	
//	// Upgrade
//	public class PotentDie extends EvolvedDie {
//		private static final int bonus = UniformDie.bonus + 5;
//		
//		public PotentDie() {
//			super(UniformDie.this);
//		}
//		
//		@Override
//		public DiceRoll onContestRoll() {
//			DiceRoll roll = roll();
//			DiceRoll result = roll.add(bonus);
//			bot.sendChannelFormat("The potent die grants the utmost magical advantage for %s's roll!",
//				owner);
//			bot.sendChannelFormat("%s rolls %s + %s = %s! %s", owner, roll, bonus, resultStr(result),
//				bot.grade(result));
//			return result;
//		}
//		
//		@Override
//		public String name() {
//			return "Potent Die";
//		}
//		
//		@Override
//		public String getUpgradeDescription() {
//			return String.format("It is now enchanted with the most potent magics, granting it +%s total roll bonus!", bonus);
//		}
//	}
}