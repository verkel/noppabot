/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.spawns.*;


public class RollingProfessional extends BasicPowerup {

	private static final int minRoll = 50;

	@Override
	public void onSpawn() {
		bot.sendChannel("A certified rolling professional appears!");
	}

	@Override
	public void onExpire() {
		bot.sendChannelFormat("... nobody seems to want to polish their skills, so the rolling professional walks away.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("The rolling professional will assist %s in tonight's roll.", ownerColored);
	}

	@Override
	public int onContestRoll(int roll) {
		if (roll >= minRoll) {
			bot.sendDefaultContestRollMessage(owner, roll);
			bot.sendChannelFormat(
				"The rolling professional compliments %s for the proper rolling technique.", owner);
			return roll;
		}
		else {
			bot.sendChannelFormat(
				"The rolling professional notices that %s's rolling technique needs work! "
					+ "%s was about to roll a lowly %d, but rolling pro shows how it's done and rolls %d!",
				owner, ownerColored, roll, minRoll);
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
	public Powerup upgrade() {
		return new RollingProfessor();
	}
	
	// Upgrade
	public class RollingProfessor extends EvolvedPowerup {
		
		private static final int minRoll = 70;
		
		public RollingProfessor() {
			super(RollingProfessional.this);
		}
		
		@Override
		public int onContestRoll(int roll) {
			if (roll >= minRoll) {
				bot.sendDefaultContestRollMessage(owner, roll);
				bot.sendChannelFormat(
					"The rolling professor gives %s an A+ for this roll!", owner);
				return roll;
			}
			else {
				bot.sendChannelFormat("The rolling professor scolds %s for not listening on his applied " +
						"dicetology lectures. %s was about to roll only %d, but the professor steps in and " +
						"demonstrates how to roll %d!",
					owner, ownerColored, roll, minRoll);
				return minRoll;
			}
		}
		
		@Override
		public String getName() {
			return "Rolling Professor";
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("The professor guarantees your roll to be at least %d.", minRoll);
		}
	}
}