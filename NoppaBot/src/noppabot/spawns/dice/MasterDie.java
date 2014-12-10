/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import java.util.*;
import java.util.Map.Entry;

import noppabot.*;
import noppabot.INoppaBot.State;
import noppabot.spawns.*;

public class MasterDie extends BasicDie {

	public static final String NAME = "The Master Die";
	private static final int sides = 150;
	
	public static final BasicPowerupSpawnInfo info = new BasicPowerupSpawnInfo() {

		@Override
		public BasicPowerup create() {
			return new MasterDie();
		}
		
		@Override
		public double spawnChance() {
			return 0.20f;
		}
	};
	
	@Override
	public SpawnInfo<?> spawnInfo() {
		return info;
	}
	
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
	public DiceRoll onContestRoll() {
		return doContestRoll(name(), sides);
	}
	
	private DiceRoll doContestRoll(String dieName, int sides) {
		DiceRoll result = bot.getRoll(owner, sides);
		bot.sendChannelFormat("%s rolls d%s with %s... %s! %s", 
			ownerColored, sides, dieName, resultStr(result), bot.grade(result));
		if (bot.getState() == State.ROLL_PERIOD) rollUnusedApprenticeDies(result);
		return result;
	}
	
	private void rollUnusedApprenticeDies(DiceRoll roll) {
		Map<String, Powerup> powerups = bot.getPowerups();
		for (Entry<String, Powerup> entry : powerups.entrySet()) {
			String owner = entry.getKey();
			Powerup powerup = entry.getValue();
			if (powerup instanceof ApprenticeDie && !bot.participated(owner)) {
				bot.sendChannelFormat("%s's apprentice die observes and then rolls %s.", 
					Color.nick(owner), resultStr(roll));
				// Don't use participate so we wont end the contest on overtime
				// and forget the master die's roll
				bot.getRolls().put(owner, roll);
			}
		}
	}

	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public int sides() {
		return sides;
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
	public class TheOneDie extends EvolvedDie {
		
		private static final int sides = 200;
		
		public TheOneDie() {
			super(MasterDie.this);
		}
		
		@Override
		public DiceRoll onContestRoll() {
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
			return String.format("Bearing %s sides, this truly is the die to rule them all.", 200);
		}
	}
}