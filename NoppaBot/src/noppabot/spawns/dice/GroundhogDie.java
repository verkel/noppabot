/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.RollRecords;
import noppabot.spawns.*;

public class GroundhogDie extends BasicPowerup {

	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", getNameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the groundhog die will now move on.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s and ensures that history will repeat itself.", 
			owner, getNameColored());
	}
	
	@Override
	public int onContestRoll(int roll) {
		Integer lastRoll = null;
		RollRecords records = bot.loadRollRecords();
		if (records != null) {
			lastRoll = records.getOrAddUser(owner).lastRolls.peekFirst();
		}
		
		if (lastRoll != null && lastRoll > 0) {
			bot.sendChannelFormat("%s throws the groundhog die with a familiar motion.", owner);
			bot.sendChannelFormat("%s rolls %d! %s", ownerColored, lastRoll, bot.grade(lastRoll));
			return lastRoll;
		}
		else {
			bot.sendChannel("The groundhog die fails to repeat yesterday's events.");
			return super.onContestRoll(roll); // Normal behaviour
		}
	}

	@Override
	public String getName() {
		return "Groundhog Die";
	}
	
	
	@Override
	public float getSpawnChance() {
		return 0.75f;
	}
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade() {
		return new SelfImprovingDie();
	}
	
	// Upgrade
	public class SelfImprovingDie extends EvolvedPowerup {
		
		private static final int bonus = 10;
		
		public SelfImprovingDie() {
			super(GroundhogDie.this);
		}
		
		@Override
		public int onContestRoll(int roll) {
			Integer lastRoll = null;
			RollRecords records = bot.loadRollRecords();
			if (records != null) {
				lastRoll = records.getOrAddUser(owner).lastRolls.peekFirst();
			}
			
			if (lastRoll != null && lastRoll > 0) {
				int result = lastRoll + bonus;
				String resultStr = resultStr(result);
				result = clamp(result);
				bot.sendChannelFormat("%s's self-improving die analyzes the yesterday's roll of %d.",
					owner, lastRoll);
				bot.sendChannelFormat("%s's self-improving die rolls %d + %d = %s! %s", 
					ownerColored, lastRoll, bonus, resultStr, bot.grade(result));
				return result;
			}
			else {
				bot.sendChannel("The self-improving die fails to improve on yesterday's events.");
				// Normal behaviour
				return super.onContestRoll(roll);
			}
		}
		
		@Override
		public String getName() {
			return "Self-Improving Die";
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("The self-improving die will roll the yesterday's roll + %d.", bonus);
		}
	}
}