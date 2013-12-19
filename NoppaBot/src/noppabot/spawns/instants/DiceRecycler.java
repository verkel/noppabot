/*
 * Created on 13.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import noppabot.spawns.dice.*;


public class DiceRecycler extends Powerup {
	@Override
	public void onSpawn() {
		bot.sendChannel("A dice recycler appears!");
	}

	@Override
	public void onExpire() {
		bot.sendChannelFormat("... the dice recycler continues to collect junk dice elsewhere.");
	}

	@Override
	public boolean canPickUp(String nick) {
		if (bot.getPowerups().containsKey(nick)) { // Has item
			return true;
		}
		else {
			bot.sendChannelFormat("%s: The recycler says you have no items to recycle.", nick);
			return false;
		}
	}
	
	@Override
	public void onPickup() {
		Powerup oldPowerup = bot.getPowerups().get(owner);
		bot.getPowerups().remove(owner);
		bot.sendChannelFormat("The recycler tosses %s's %s into a peculiar shredder machine. " +
			"One moment later, something pops out:", owner, oldPowerup);
		bot.scheduleRandomSpawn(null, Powerups.firstPowerup, null);
	}

	@Override
	public boolean isCarried() {
		return false;
	}
	
	@Override
	public String getName() {
		return "Dice Recycler";
	}
}
