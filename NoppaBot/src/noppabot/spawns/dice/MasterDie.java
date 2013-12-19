/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import java.util.*;
import java.util.Map.Entry;

import noppabot.INoppaBot;


public class MasterDie extends Powerup {

	private static final int sides = 150;
	
	@Override
	public void onSpawn() {
		bot.sendChannel("The Master Die appears!");
	}

	@Override
	public void onExpire() {
		bot.sendChannelFormat("... nobody took the Master Die?! Maybe you should learn some " +
			"dice appraisal skills at the certified rolling professional.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat(
			"%s wrestles for the Master Die with the other contestants and barely comes on top. " +
			"Surely the Master Die must be worth all the trouble!",
			ownerColored);
		
		bot.insertApprenticeDice();
	}

	@Override
	public int onContestRoll(int roll) {
		return doContestRoll(getName(), sides);
	}
	
	private int doContestRoll(String dieName, int sides) {
		int result = bot.getRollFor(owner, sides);
		String resultStr = rollToString(bot, result);
		result = clamp(bot, result);
		bot.sendChannelFormat("%s rolls d%d with %s... %s! %s", 
			ownerColored, sides, dieName, resultStr, bot.grade(result));
		rollUnusedApprenticeDies(bot, result);
		return result;
	}
	
	private static void rollUnusedApprenticeDies(INoppaBot bot, int roll) {
		Map<String, Powerup> powerups = bot.getPowerups();
		for (Entry<String, Powerup> entry : powerups.entrySet()) {
			String owner = entry.getKey();
			Powerup powerup = entry.getValue();
			if (powerup instanceof ApprenticeDie && !bot.participated(owner)) {
				bot.sendChannelFormat("%s's apprentice die observes and then rolls %d.", owner, roll);
				// Don't use participate so we wont end the contest on overtime
				// and forget the master die's roll
				bot.getRolls().put(owner, roll);
			}
		}
	}

	@Override
	public String getName() {
		return "The Master Die";
	}
	
	@Override
	public float getSpawnChance() {
		return 0.25f;
	}
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade() {
		return new TheOneDie();
	}
	
	// Upgrade
	public class TheOneDie extends Powerup {
		
		private static final int sides = 200;
		
		@Override
		public int onContestRoll(int roll) {
			return doContestRoll(getName(), sides);
		}
		
		@Override
		public String getName() {
			return "The One Die";
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("Bearing %d sides, this truly is the die to rule them all.", 200);
		}
	}
}