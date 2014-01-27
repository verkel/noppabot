/*
 * Created on 13.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import noppabot.Color;
import noppabot.spawns.*;


public class DiceRecycler extends Instant {
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", nameColored());
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
			bot.sendChannelFormat("%s: The recycler says you have no items to recycle.", Color.nick(nick));
			return false;
		}
	}
	
	@Override
	public void onPickup() {
		Powerup oldPowerup = bot.getPowerups().get(owner);
		bot.getPowerups().remove(owner);
		bot.sendChannelFormat("The recycler tosses %s's %s into a peculiar shredder machine. " +
			"One moment later, something pops out:", ownerColored, oldPowerup.nameColored());
		bot.scheduleRandomSpawn(null, Powerups.firstPowerup, null);
	}

	@Override
	public String name() {
		return "Dice Recycler";
	}
}
