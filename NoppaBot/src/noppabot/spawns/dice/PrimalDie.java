/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import java.util.*;

import noppabot.INoppaBot;

public class PrimalDie extends Powerup {

	private static final Set<Integer> primes = new HashSet<Integer>();
	
	public static final int bonus = 20;

	static {
		primes.addAll(Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59,
			61, 67, 71, 73, 79, 83, 89, 97));
	}
	
	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("A primal die appears!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... nobody wanted the primal die. Now it disappears.");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat("%s grabs the primal die and gains knowledge about prime numbers.",
			nick);
	}

	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		if (primes.contains(roll)) {
			int result = roll + bonus;
			result = clamp(result);
			bot.sendChannelFormat(
				"%s rolls %d, which is a prime! The primal die is pleased, and adds a bonus to the roll. The final roll is %d + %d = %d.",
				nick, roll, roll, bonus, result);
			return result;
		}
		else {
			bot.sendChannelFormat(
				"%s rolls %d! It's not a prime, however, and the primal die disapproves.", nick,
				roll);
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
	public Powerup upgrade(INoppaBot bot) {
		return new TribalDie();
	}
	
	// Upgrade
	public static class TribalDie extends Powerup {
		
		private static final int otherPrimesBonus = 10;
		private int otherPrimesTotalBonus = 0;
		
		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			int result = roll;
			if (primes.contains(roll)) {
				result += bonus;
				bot.sendChannelFormat(
					"%s rolls %d, which is a prime! The tribe of primal dies grants you an offering of" +
					" bonus points. The modified roll is %d + %d = %d.",
					nick, roll, roll, bonus, result);
			}
			else {
				bot.sendChannelFormat(
					"%s rolls %d! The tribe of primal dice seem uninterested.", nick,
					roll);
			}
			
			if (otherPrimesTotalBonus > 0) {
				result += otherPrimesTotalBonus;
				bot.sendChannelFormat("The tribal gifts of %d points are added to %s's roll, " +
					"increasing it to %d", otherPrimesTotalBonus, nick, result);
			}
			
			result = clamp(result);
			return result;
		}
		
		@Override
		public void onOpponentRollLate(INoppaBot bot, String owner, String opponent, int roll) {
			Map<String, Integer> rolls = bot.getRolls();
			
			if (primes.contains(roll)) {
				if (bot.participated(owner)) {
					int totalRoll = rolls.get(owner) + otherPrimesBonus;
					rolls.put(owner, totalRoll);
					bot.sendChannelFormat("%s's roll %d is a prime, and excitement in the primal dice tribe grows. " +
						"%s gets an offering of %d points from the tribe! %s's roll is now %d.",
						opponent, roll, owner, otherPrimesBonus, owner, totalRoll);
				}
				else {
					otherPrimesTotalBonus += otherPrimesBonus;
					bot.sendChannelFormat("%s's roll %d is a prime, and excitement in the primal dice tribe grows. " +
						"%s gets an offering of %d points from the tribe!",
						opponent, roll,  owner, otherPrimesBonus);
				}
			}
		}
		
		@Override
		public String getName() {
			return "Tribal Die";
		}
		
		@Override
		public String getUpgradeDescription(INoppaBot bot, String nick) {
			return String.format("The whole tribe of primal dies now support you, and will grant" +
				" %d bonus for every prime rolled by an opponent.", otherPrimesBonus);
		}
	}
}