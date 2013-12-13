/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.INoppaBot;


public class ExtremeDie extends Powerup {

	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("The extreme die appears!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... the extreme die gets bored sitting still and takes off.");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat(
			"%s grabs the extreme die! You discuss about various subcultures popular with radical dice.",
			nick);
	}

	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		return doContestRoll(getName(), 100, bot, nick, roll);
	}

	private static int doContestRoll(String dieName, int sides, INoppaBot bot, String nick, int roll) {
		if (roll == 100) {
			bot.sendChannelFormat("%s rolls d%d with the extreme die! %d! That's the most extreme roll and the die is ecstatic!", nick, sides, roll); 
			return roll;
		}
		else if (roll > 10 && roll < 90) {
			bot.sendChannelFormat("%s rolls d%d with the extreme die! %d! This number is quite ordinary, says the die.", nick, sides, roll);
			return roll;
		}
		else {
			bot.sendChannelFormat("%s rolls d%d with the extreme die! %d! This number is very extremal! says the die.", nick, sides, roll);
			bot.sendChannelFormat("The extreme die rewards %s with the most extreme roll, 100!", nick);
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
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			int newRoll = bot.getRollFor(nick, sides);
			return doContestRoll(getName(), sides, bot, nick, newRoll);
		}
		
		@Override
		public String getName() {
			return "Daring Die";
		}
		
		@Override
		public String getUpgradeDescription(INoppaBot bot, String nick) {
			return String.format("Instead of d100, you now roll d%d.", sides);
		}
	}
}
