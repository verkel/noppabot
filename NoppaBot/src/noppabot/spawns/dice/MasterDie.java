/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import java.util.*;
import java.util.Map.Entry;

import noppabot.ColorStr;
import noppabot.spawns.*;


public class MasterDie extends BasicPowerup {

	private static final int sides = 150;
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("%s appears!", nameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... nobody took the Master Die?! Maybe you should learn some " +
			"dice appraisal skills at the certified rolling professional.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat(
			"%s wrestles for %s with the other contestants and barely comes on top. " +
			"Surely the Master Die must be worth all the trouble!",
			ownerColored, nameColored());
		
		bot.insertApprenticeDice();
	}

	@Override
	public int onContestRoll() {
		return doContestRoll(name(), sides);
	}
	
	private int doContestRoll(String dieName, int sides) {
		int result = bot.getRoll(owner, sides);
		String resultStr = resultStr(result);
		result = clamp(result);
		bot.sendChannelFormat("%s rolls d%d with %s... %s! %s", 
			ownerColored, sides, dieName, resultStr, bot.grade(result));
		rollUnusedApprenticeDies(result);
		return result;
	}
	
	private void rollUnusedApprenticeDies(int roll) {
		Map<String, Powerup> powerups = bot.getPowerups();
		for (Entry<String, Powerup> entry : powerups.entrySet()) {
			String owner = entry.getKey();
			Powerup powerup = entry.getValue();
			if (powerup instanceof ApprenticeDie && !bot.participated(owner)) {
				bot.sendChannelFormat("%s's apprentice die observes and then rolls %s.", 
					ColorStr.nick(owner), resultStr(roll));
				// Don't use participate so we wont end the contest on overtime
				// and forget the master die's roll
				bot.getRolls().put(owner, roll);
			}
		}
	}

	@Override
	public String name() {
		return "The Master Die";
	}
	
	@Override
	public int sides() {
		return sides;
	}
	
	@Override
	public float spawnChance() {
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
	public class TheOneDie extends EvolvedPowerup {
		
		private static final int sides = 200;
		
		public TheOneDie() {
			super(MasterDie.this);
		}
		
		@Override
		public int onContestRoll() {
			return doContestRoll(name(), sides);
		}
		
		@Override
		public String name() {
			return "The One Die";
		}
		
		@Override
		public int sides() {
			return sides;
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("Bearing %d sides, this truly is the die to rule them all.", 200);
		}
	}
}