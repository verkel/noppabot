/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.DiceRoll;
import noppabot.spawns.*;


public class SwapperDie extends BasicDie {

	public static final BasicPowerupSpawnInfo info = new BasicPowerupSpawnInfo() {

		@Override
		public BasicPowerup create() {
			return new SwapperDie();
		}
	};
	
	@Override
	public SpawnInfo<?> spawnInfo() {
		return info;
	}
	
	public static final int bonus = 15;

	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", nameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the swapper die swaps a tree stump into its place.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s swaps the %s into the inventory.", 
			ownerColored, nameColored());
	}
	
	@Override
	public DiceRoll onContestRoll() {
		DiceRoll roll = roll();
		char[] chars = roll.toString().toCharArray();
		
		if (chars.length == 2) {
			String resultRollStr = String.valueOf(chars[1]) + String.valueOf(chars[0]);
			int resultRollInt = Integer.parseInt(resultRollStr);
			DiceRoll result = new DiceRoll(resultRollInt);
			bot.sendChannelFormat(
				"%s rolls %s with the swapper die. The die swaps the digits of your roll!",
				owner, roll);
			bot.sendChannelFormat("%s's roll result is set to %s!", ownerColored, resultStr(result));
			return result;
		}
		else {
			bot.sendChannelFormat(
				"%s rolls %s with the swapper die. The die doesn't seem to able to swap the digits of this roll.",
				ownerColored, resultStr(roll));
			return roll;
		}
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
	public String name() {
		return "Swapper Die";
	}
	
	@Override
	public Powerup upgrade() {
		return new UniformDie();
	}
	
	// Upgrade
	public class UniformDie extends EvolvedDie {
		public UniformDie() {
			super(SwapperDie.this);
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
					"%s rolls %s with the uniform die. The distance of your roll's digits is %s.",
					owner, roll, distance);
				bot.sendChannelFormat("%s's roll result is set to (10 - %s) * %s = %s!", ownerColored, distance, 10,
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
		public String getUpgradeDescription() {
			return String.format("The swapper die puts on its special uniform! Now your roll result will"
				+ " depend on how close the digits of your original roll are.");
		}
	}
}