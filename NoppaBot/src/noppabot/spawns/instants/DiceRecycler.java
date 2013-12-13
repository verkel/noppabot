/*
 * Created on 13.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import noppabot.INoppaBot;
import noppabot.spawns.dice.*;


public class DiceRecycler extends Powerup {
	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("A dice recycler appears!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... the dice recycler continues to collect junk dice elsewhere.");
	}

	@Override
	public boolean canPickUp(INoppaBot bot, String nick) {
		if (bot.getPowerups().containsKey(nick)) { // Has item
			return true;
		}
		else {
			bot.sendChannelFormat("%s: The recycler says you have no items to recycle.", nick);
			return false;
		}
	}
	
	@Override
	public void onPickup(INoppaBot bot, String nick) {
		Powerup oldPowerup = bot.getPowerups().get(nick);
		bot.getPowerups().remove(nick);
		bot.sendChannelFormat("The recycler tosses %s's %s into a peculiar shredder machine. " +
			"One moment later, something pops out:", nick, oldPowerup);
		bot.scheduleSpawn(null, Powerups.firstPowerup, null);
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
