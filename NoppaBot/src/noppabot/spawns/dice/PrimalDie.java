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
			result = capResult(result);
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
}