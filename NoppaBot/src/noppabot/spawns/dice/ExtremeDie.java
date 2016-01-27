/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.DiceRoll;
import noppabot.spawns.*;

public class ExtremeDie extends BasicDie {
	
	private static final int sides = 100;
	private static final int highRollBonusSides = 10;

	public static final BasicPowerupSpawnInfo info = new BasicPowerupSpawnInfo() {

		@Override
		public BasicPowerup create() {
			return new ExtremeDie();
		}
		
	};
	
	@Override
	public SpawnInfo<?> spawnInfo() {
		return info;
	}
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("An %s appears!", nameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the extreme die gets bored sitting still and takes off.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat(
			"%s grabs the %s! You discuss about various subcultures popular with radical dice.",
			ownerColored, nameColored());
	}

	@Override
	public DiceRoll onContestRoll() {
		return doContestRoll(name(), sides);
	}

	private DiceRoll doContestRoll(String dieName, int sides) {
		DiceRoll roll = roll(sides);
		if (roll.total() == 100) {
			bot.sendChannelFormat("%s rolls d%s with the %s! %s! That's the most extreme roll and the die is ecstatic!", 
				ownerColored, sides, dieName, resultStr(roll)); 
			return roll;
		}
		else if (roll.total() > 10 && roll.total() < 90) {
			bot.sendChannelFormat("%s rolls d%s with the %s! %s! This number is quite ordinary, says the die.", 
				ownerColored, sides, dieName, resultStr(roll));
			return roll;
		}
		else if (roll.total() <= 10) {
			DiceRoll result = new DiceRoll(100).sub(roll);
			bot.sendChannelFormat("%s rolls d%s with the %s! %s! This number is very extremal! says the die.", 
				owner, sides, dieName, roll);
			bot.sendChannelFormat("The %s flips %s's roll to the other extreme, it's now 100 - %s = %s!",
				dieName, ownerColored, roll, resultStr(result));
			return result;
		}
		else if (roll.total() >= 90) {
			DiceRoll bonus = roll(highRollBonusSides);
			DiceRoll result = roll.add(bonus);
			bot.sendChannelFormat("%s rolls d%s with the %s! %s! This number is very extremal! says the die.", 
				owner, sides, dieName, roll);
			bot.sendChannelFormat("The %s rewards %s with d%s bonus to the roll: %s + %s = %s!",
				dieName, ownerColored, highRollBonusSides, roll, bonus, resultStr(result));
			return result;
		}
		throw new IllegalStateException();
	}

	@Override
	public String name() {
		return "Extreme Die";
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
		return new DaringDie();
	}
	
	// Upgrade
	public class DaringDie extends EvolvedDie {
		
		private static final int sides = 30;
		
		public DaringDie() {
			super(ExtremeDie.this);
		}
		
		@Override
		public DiceRoll onContestRoll() {
			return doContestRoll(name(), sides);
		}
		
		@Override
		public String name() {
			return "Daring Die";
		}
		
		@Override
		public int sides() {
			return sides;
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("Instead of d100, you now roll d%s.", sides);
		}
	}
}
