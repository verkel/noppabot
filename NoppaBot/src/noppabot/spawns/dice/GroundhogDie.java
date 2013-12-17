/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.*;

public class GroundhogDie extends Powerup {

	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("A groundhog die appears!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... the groundhog die will now move on.");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat("%s grabs the groundhog die and ensures that history will repeat itself.", nick);
	}
	
	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		Integer lastRoll = null;
		RollRecords records = bot.loadRollRecords();
		if (records != null) {
			lastRoll = records.getOrAddUser(nick).lastRolls.peekFirst();
		}
		
		if (lastRoll != null && lastRoll > 0) {
			bot.sendChannelFormat("%s throws the groundhog die with a familiar motion.", nick);
			bot.sendChannelFormat("%s rolls %d! %s", nick, lastRoll, bot.grade(lastRoll));
			return lastRoll;
		}
		else {
			bot.sendChannel("The groundhog die fails to repeat yesterday's events.");
			return super.onContestRoll(bot, nick, roll); // Normal behaviour
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
	public Powerup upgrade(INoppaBot bot) {
		return new SelfImprovingDie();
	}
	
	// Upgrade
	public static class SelfImprovingDie extends Powerup {
		
		private static final int bonus = 10;
		
		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			Integer lastRoll = null;
			RollRecords records = bot.loadRollRecords();
			if (records != null) {
				lastRoll = records.getOrAddUser(nick).lastRolls.peekFirst();
			}
			
			if (lastRoll != null && lastRoll > 0) {
				int result = lastRoll + bonus;
				result = clamp(bot, result);
				bot.sendChannelFormat("%s's self-improving die analyzes the yesterday's roll of %d.",
					nick, lastRoll);
				bot.sendChannelFormat("%s's self-improving die rolls %d + %d = %d! %s", 
					nick, lastRoll, bonus, result, bot.grade(result));
				return result;
			}
			else {
				bot.sendChannel("The self-improving die fails to improve on yesterday's events.");
				return super.onContestRoll(bot, nick, roll); // Normal behaviour
			}
		}
		
		@Override
		public String getName() {
			return "Self-Improving Die";
		}
		
		@Override
		public String getUpgradeDescription(INoppaBot bot, String nick) {
			return String.format("The self-improving die will roll the yesterday's roll + %d.", bonus);
		}
	}
}