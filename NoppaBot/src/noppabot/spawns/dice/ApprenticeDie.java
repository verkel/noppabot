/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.spawns.*;


public class ApprenticeDie extends BasicPowerup {

	@Override
	public void onSpawn() {
		bot.sendChannelFormat("An %s appears!", getNameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the apprentice die will find no masters today.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s and it looks forward to learning from the Master Die.", 
			ownerColored, getNameColored());
	}
	
	@Override
	public int onContestRoll() {
		bot.sendChannelFormat("%s rolls with the apprentice die, but it really just wanted " +
			"to see the Master Die do it, first.", ownerColored);
		return super.onContestRoll(); // Normal behaviour
	}
	
	@Override
	public void onTiebreakPeriodStart() {
		bot.sendChannelFormat("%s's apprentice die has learned enough and evolves into a Master Die!", ownerColored);
		bot.getPowerups().put(owner, new MasterDie());
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