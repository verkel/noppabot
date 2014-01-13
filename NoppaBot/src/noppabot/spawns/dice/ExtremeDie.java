/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.spawns.*;



public class ExtremeDie extends BasicPowerup {

	@Override
	public void onSpawn() {
		bot.sendChannelFormat("An %s appears!", getNameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the extreme die gets bored sitting still and takes off.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat(
			"%s grabs the %s! You discuss about various subcultures popular with radical dice.",
			ownerColored, getNameColored());
	}

	@Override
	public int onContestRoll() {
		return doContestRoll(getName(), 100);
	}

	private int doContestRoll(String dieName, int sides) {
		int roll = roll(sides);
		if (roll == 100) {
			bot.sendChannelFormat("%s rolls d%d with the extreme die! %s! That's the most extreme roll and the die is ecstatic!", 
				ownerColored, sides, resultStr(roll)); 
			return roll;
		}
		else if (roll > 10 && roll < 90) {
			bot.sendChannelFormat("%s rolls d%d with the extreme die! %s! This number is quite ordinary, says the die.", 
				ownerColored, sides, resultStr(roll));
			return roll;
		}
		else {
			bot.sendChannelFormat("%s rolls d%d with the extreme die! %d! This number is very extremal! says the die.", 
				owner, sides, roll);
			bot.sendChannelFormat("The extreme die rewards %s with the most extreme roll, %s!",
				ownerColored, resultStr(100));
			return 100;
		}
	}

	@Override
	public String getName() {
		return "Extreme Die";
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
			return doContestRoll(getName(), sides);
		}
		
		@Override
		public String getName() {
			return "Daring Die";
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("Instead of d100, you now roll d%d.", sides);
		}
	}
}
