/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import java.util.*;

import noppabot.Rolls;
import noppabot.spawns.*;

public class PrimalDie extends BasicPowerup {

	private static final Set<Integer> primes = new HashSet<Integer>();
	
	public static final int bonus = 20;

	static {
		primes.addAll(Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59,
			61, 67, 71, 73, 79, 83, 89, 97));
	}
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", getNameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... nobody wanted the primal die. Now it disappears.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s and gains knowledge about prime numbers.",
			ownerColored, getNameColored());
	}

	@Override
	public int onContestRoll(int roll) {
		if (primes.contains(roll)) {
			int result = roll + bonus;
			String resultStr = resultStr(result);
			result = clamp(result);
			bot.sendChannelFormat(
				"%s rolls %d, which is a prime! The primal die is pleased, and adds a bonus to the roll. The final roll is %d + %d = %s.",
				ownerColored, roll, roll, bonus, resultStr);
			return result;
		}
		else {
			bot.sendChannelFormat(
				"%s rolls %s! It's not a prime, however, and the primal die disapproves.", ownerColored,
				resultStr(roll));
			return roll;
		}
	}

	@Override
	public String getName() {
		return "Primal Die";
	}
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade() {
		return new TribalDie();
	}
	
	// Upgrade
	public class TribalDie extends EvolvedPowerup {
		
		private static final int otherPrimesBonus = 10;
		private int otherPrimesTotalBonus = 0;
		
		public TribalDie() {
			super(PrimalDie.this);
		}
		
		@Override
		public int onContestRoll(int roll) {
			int result = roll;
			if (primes.contains(roll)) {
				result += bonus;
				String resultStr = resultStr(result);
				result = clamp(result);
				bot.sendChannelFormat(
					"%s rolls %d, which is a prime! The tribe of primal dies grants you an offering of" +
					" bonus points. The modified roll is %d + %d = %s.",
					ownerColored, roll, roll, bonus, resultStr);
			}
			else {
				bot.sendChannelFormat(
					"%s rolls %s! The tribe of primal dice seem uninterested.", ownerColored,
					resultStr(roll));
			}
			
			if (otherPrimesTotalBonus > 0) {
				result += otherPrimesTotalBonus;
				String resultStr = resultStr(result);
				result = clamp(result);
				bot.sendChannelFormat("The tribal gifts of %d points are added to %s's roll, " +
					"increasing it to %s", otherPrimesTotalBonus, ownerColored, resultStr);
			}
			
			return result;
		}
		
		@Override
		public void onOpponentRollLate(String opponent, int roll) {
			Rolls rolls = bot.getRolls();
			
			if (primes.contains(roll)) {
				if (bot.participated(owner)) {
					int totalRoll = rolls.get(owner) + otherPrimesBonus;
					String totalRollStr = resultStr(totalRoll);
					totalRoll = clamp(totalRoll);
					rolls.put(owner, totalRoll);
					bot.sendChannelFormat("%s's roll %d is a prime, and excitement in the primal dice tribe grows. " +
						"%s gets an offering of %d points from the tribe! %s's roll is now %s.",
						opponent, roll, owner, otherPrimesBonus, ownerColored, totalRollStr);
				}
				else {
					otherPrimesTotalBonus += otherPrimesBonus;
					bot.sendChannelFormat("%s's roll %d is a prime, and excitement in the primal dice tribe grows. " +
						"%s gets an offering of %d points from the tribe!",
						opponent, roll, ownerColored, otherPrimesBonus);
				}
			}
		}
		
		@Override
		public String getName() {
			return "Tribal Die";
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("The whole tribe of primal dies now support you, and will grant" +
				" %d bonus for every prime rolled by an opponent.", otherPrimesBonus);
		}
	}
}