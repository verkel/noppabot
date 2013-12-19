/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import noppabot.spawns.Instant;


public class Diceteller extends Instant {

	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", getNameColored());
	}

	@Override
	public void onExpire() {
		bot.sendChannelFormat("... no one seemed to believe the diceteller could see future rolls in a crystal ball. " +
			"The ball was of wrong shape, anyway. Maybe next time she brings crystal dice?");
	}

	@Override
	public void onPickup() {
		int result = bot.peekRollFor(owner);
		bot.sendChannelFormat(
			"The diceteller, glancing at his crystal ball, whispers to %s: \"Your next roll will be %s.\"",
			ownerColored, resultStr(result));
	}

	@Override
	public String getName() {
		return "Diceteller";
	}
	
	@Override
	public float getSpawnChance() {
		return 2.0f;
	}
}

