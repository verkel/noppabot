/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import java.util.*;

import noppabot.*;
import noppabot.spawns.*;

public class PrimalDie extends BasicDie {

	public static final Set<Integer> primes = new HashSet<Integer>();
	
	public static final int bonus = 20;

	static {
		primes.addAll(Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59,
			61, 67, 71, 73, 79, 83, 89, 97));
	}
	
	public static final BasicPowerupSpawnInfo info = new BasicPowerupSpawnInfo() {

		@Override
		public BasicPowerup create() {
			return new PrimalDie();
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
		sendExpireMessageFormat("... nobody wanted the primal die. Now it disappears.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s and gains knowledge about prime numbers.",
			ownerColored, nameColored());
	}

	@Override
	public DiceRoll onContestRoll() {
		final DiceRoll roll = roll();
		if (primes.contains(roll.total())) {
			DiceRoll result = roll.add(bonus);
			bot.sendChannelFormat(
				"%s rolls %s, which is a prime! The primal die is pleased, and adds a bonus to the roll. The final roll is %s + %s = %s.",
				ownerColored, roll, roll, bonus, resultStr(result));
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
	public String name() {
		return "Primal Die";
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
		return new TribalDie();
	}
	
	// Upgrade
	public class TribalDie extends EvolvedDie {
		
		private static final int otherPrimesBonus = 10;
		private int otherPrimesTotalBonus = 0;
		
		public TribalDie() {
			super(PrimalDie.this);
		}
		
		@Override
		public DiceRoll onContestRoll() {
			final DiceRoll roll = roll();
			DiceRoll result;
			if (primes.contains(roll.total())) {
				result = roll.add(bonus);
				bot.sendChannelFormat(
					"%s rolls %s, which is a prime! The tribe of primal dies grants you an offering of" +
					" bonus points. The modified roll is %s + %s = %s.",
					ownerColored, roll, roll, bonus, resultStr(result));
			}
			else {
				result = roll;
				bot.sendChannelFormat(
					"%s rolls %s! The tribe of primal dice seem uninterested.", ownerColored,
					resultStr(roll));
			}
			
			if (otherPrimesTotalBonus > 0) {
				result = result.add(otherPrimesTotalBonus);
				bot.sendChannelFormat("The tribal gifts of %s points are added to %s's roll, " +
					"increasing it to %s", otherPrimesTotalBonus, ownerColored, resultStr(result));
			}
			
			return result;
		}
		
		@Override
		public void onOpponentRollLate(String opponent, Roll roll) {
			Rolls rolls = bot.getRolls();
			
			if (primes.contains(roll.total())) {
				if (bot.participated(owner)) {
					DiceRoll totalRoll = ((DiceRoll)rolls.get(owner)).add(otherPrimesBonus);
					rolls.put(owner, totalRoll);
					bot.sendChannelFormat("%s's roll %s is a prime, and excitement in the primal dice tribe grows. " +
						"%s gets an offering of %s points from the tribe! %s's roll is now %s.",
						opponent, roll, owner, otherPrimesBonus, ownerColored, resultStr(totalRoll));
				}
				else {
					otherPrimesTotalBonus += otherPrimesBonus;
					bot.sendChannelFormat("%s's roll %s is a prime, and excitement in the primal dice tribe grows. " +
						"%s gets an offering of %s points from the tribe!",
						opponent, roll, ownerColored, otherPrimesBonus);
				}
			}
		}
		
		@Override
		public String name() {
			return "Tribal Die";
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("The whole tribe of primal dies now support you, and will grant" +
				" %s bonus for every prime rolled by an opponent.", otherPrimesBonus);
		}
	}
}