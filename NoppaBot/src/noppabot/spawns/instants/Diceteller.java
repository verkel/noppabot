/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import noppabot.INoppaBot;
import noppabot.spawns.dice.Powerup;


public class Diceteller extends Powerup {

	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("A Diceteller appears!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... no one seemed to believe the diceteller could see future rolls in a crystal ball. " +
			"The ball was of wrong shape, anyway. Maybe next time she brings crystal dice?");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		int result = bot.peekRollFor(nick);
		bot.sendChannelFormat(
			"The diceteller, glancing at his crystal ball, whispers to %s: \"Your next roll will be %d.\"",
			nick, result);
	}

	@Override
	public boolean isCarried() {
		return false;
	}
	
	@Override
	public String getName() {
		return "Diceteller";
	}
}

