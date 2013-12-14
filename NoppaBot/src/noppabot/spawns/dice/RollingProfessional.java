/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.INoppaBot;

public class RollingProfessional extends Powerup {

	private static final int minRoll = 50;

	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("A certified rolling professional appears!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... nobody seems to want to polish their skills, so the rolling professional walks away.");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat("The rolling professional will assist %s in tonight's roll.", nick);
	}

	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		if (roll >= minRoll) {
			bot.sendDefaultContestRollMessage(nick, roll);
			bot.sendChannelFormat(
				"The rolling professional compliments %s for the proper rolling technique.", nick);
			return roll;
		}
		else {
			bot.sendChannelFormat(
				"The rolling professional notices that %s's rolling technique needs work! "
					+ "%s was about to roll a lowly %d, but rolling pro shows how it's done and rolls %d!",
				nick, nick, roll, minRoll);
			return minRoll;
		}
	}

	@Override
	public String getName() {
		return "Rolling Professional";
	}	
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade(INoppaBot bot) {
		return new RollingProfessor();
	}
	
	// Upgrade
	public static class RollingProfessor extends Powerup {
		
		private static final int minRoll = 70;
		
		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			if (roll >= minRoll) {
				bot.sendDefaultContestRollMessage(nick, roll);
				bot.sendChannelFormat(
					"The rolling professor gives you an A+ for this roll!", nick);
				return roll;
			}
			else {
				bot.sendChannelFormat("The rolling professor scolds %s for not listening on his applied " +
						"dicetology lectures. %s was about to roll only %d, but the professor steps in and " +
						"demonstrates how to roll %d!",
					nick, nick, roll, minRoll);
				return minRoll;
			}
		}
		
		@Override
		public String getName() {
			return "Rolling Professor";
		}
		
		@Override
		public String getUpgradeDescription(INoppaBot bot, String nick) {
			return String.format("The professor guarantees your roll to be at least %d.", minRoll);
		}
	}
}