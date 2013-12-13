/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.INoppaBot;

public class ApprenticeDie extends Powerup {

	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("An apprentice die appears!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... the apprentice die will find no masters today.");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat("%s grabs the apprentice die and it looks forward to learning from the Master Die.", nick);
	}
	
	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		bot.sendChannelFormat("%s rolls with the apprentice die, but it really just wanted " +
			"to see the Master Die do it, first.", nick);
		return super.onContestRoll(bot, nick, roll); // Normal behaviour
	}
	
	@Override
	public void onTiebreakPeriodStart(INoppaBot bot, String nick) {
		bot.sendChannelFormat("%s's apprentice die has learned enough and evolves into a Master Die!", nick);
		bot.getPowerups().put(nick, new MasterDie());
	}

	@Override
	public String getName() {
		return "Apprentice Die";
	}
	
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade() {
		return new MasterDie();
	}
}