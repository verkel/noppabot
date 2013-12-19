/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.INoppaBot;


public class ExtremeDie extends Powerup {

	@Override
	public void onSpawn() {
		bot.sendChannel("The extreme die appears!");
	}

	@Override
	public void onExpire() {
		bot.sendChannelFormat("... the extreme die gets bored sitting still and takes off.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat(
			"%s grabs the extreme die! You discuss about various subcultures popular with radical dice.",
			ownerColored);
	}

	@Override
	public int onContestRoll(int roll) {
		return doContestRoll(getName(), 100, bot, ownerColored, roll);
	}

	private static int doContestRoll(String dieName, int sides, INoppaBot bot, String ownerColored, int roll) {
		if (roll == 100) {
			bot.sendChannelFormat("%s rolls d%d with the extreme die! %d! That's the most extreme roll and the die is ecstatic!", ownerColored, sides, roll); 
			return roll;
		}
		else if (roll > 10 && roll < 90) {
			bot.sendChannelFormat("%s rolls d%d with the extreme die! %d! This number is quite ordinary, says the die.", ownerColored, sides, roll);
			return roll;
		}
		else {
			bot.sendChannelFormat("%s rolls d%d with the extreme die! %d! This number is very extremal! says the die.", ownerColored, sides, roll);
			bot.sendChannelFormat("The extreme die rewards %s with the most extreme roll, 100!", ownerColored);
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
	public static class DaringDie extends Powerup {
		
		private static final int sides = 30;
		
		@Override
		public int onContestRoll(int roll) {
			int newRoll = bot.getRollFor(owner, sides);
			return doContestRoll(getName(), sides, bot, owner, newRoll);
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
