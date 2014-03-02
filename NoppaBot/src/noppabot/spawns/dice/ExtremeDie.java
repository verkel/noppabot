/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.spawns.*;



public class ExtremeDie extends BasicPowerup {
	
	private static final int sides = 100;
	private static final int highRollBonusSides = 10;

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
	public int onContestRoll() {
		return doContestRoll(name(), sides);
	}

	private int doContestRoll(String dieName, int sides) {
		int roll = roll(sides);
		if (roll == 100) {
			bot.sendChannelFormat("%s rolls d%d with the %s! %s! That's the most extreme roll and the die is ecstatic!", 
				ownerColored, sides, dieName, resultStr(roll)); 
			return roll;
		}
		else if (roll > 10 && roll < 90) {
			bot.sendChannelFormat("%s rolls d%d with the %s! %s! This number is quite ordinary, says the die.", 
				ownerColored, sides, dieName, resultStr(roll));
			return roll;
		}
		else if (roll <= 10) {
			int result = 100 - roll;
			bot.sendChannelFormat("%s rolls d%d with the %s! %d! This number is very extremal! says the die.", 
				owner, sides, dieName, roll);
			bot.sendChannelFormat("The %s flips %s's roll to the other extreme, it's now 100 - %d = %s!",
				dieName, ownerColored, roll, resultStr(result));
			return result;
		}
		else if (roll >= 90) {
			int bonus = roll(highRollBonusSides);
			int result = roll + bonus;
			String resultStr = resultStr(result);
			result = clamp(result);
			bot.sendChannelFormat("%s rolls d%d with the %s! %d! This number is very extremal! says the die.", 
				owner, sides, dieName, roll);
			bot.sendChannelFormat("The %s rewards %s with d%d bonus to the roll: %d + %d = %s!",
				dieName, ownerColored, highRollBonusSides, roll, bonus, resultStr);
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
	public class DaringDie extends EvolvedPowerup {
		
		private static final int sides = 30;
		
		public DaringDie() {
			super(ExtremeDie.this);
		}
		
		@Override
		public int onContestRoll() {
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
			return String.format("Instead of d100, you now roll d%d.", sides);
		}
	}
}
