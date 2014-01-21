/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import noppabot.spawns.Instant;


public class Diceteller extends Instant {

	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", nameColored());
	}

	@Override
	public void onExpire() {
		bot.sendChannelFormat("... no one seemed to believe the diceteller could see future rolls in a crystal ball. " +
			"The ball was of wrong shape, anyway. Maybe next time she brings crystal dice?");
	}

	@Override
	public void onPickup() {
		int sides = bot.getPowerupSides(owner);
		int result = bot.peekRoll(owner, sides, true);
		bot.sendChannelFormat(
			"The %s, glancing at his crystal ball, whispers to %s: \"Your next roll of d%d will be %s.\"",
			nameColored(), ownerColored, sides, resultStr(result));
	}

	@Override
	public String name() {
		return "Diceteller";
	}
	
	@Override
	public float spawnChance() {
		return 2.0f;
	}
}

