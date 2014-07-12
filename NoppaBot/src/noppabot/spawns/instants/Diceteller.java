/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import noppabot.*;
import noppabot.PeekedRoll.Hint;
import noppabot.spawns.Instant;


public class Diceteller extends Instant {
	
	public static final String NAME = "Diceteller";

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
	public boolean canPickUp(String nick, boolean verbose) {
		if (super.canPickUp(nick, verbose)) {
			PeekableRandom random = bot.getRandomFor(nick);
			int sides = bot.getPowerupSides(nick);
			if (random.peek(sides).canSpawnHint()) {
				return true;
			}
			else {
				if (verbose) bot.sendChannelFormat("%s: you already know your next roll of %s!", 
					Color.nick(nick), getDieSidesStr(sides));
				return false;
			}
		}
		return false;
	}

	@Override
	public void onPickup() {
		int sides = bot.getPowerupSides(owner);
		Hint hint = bot.spawnRollHint(owner, sides);
		bot.sendChannelFormat(
			"The %s, glancing at %s's %s, whispers: \"%s\"",
			nameColored(), ownerColored, getDieSidesStr(sides), hint.tell());
		bot.sendChannelFormat("actual: %d", bot.peekRoll(owner, sides).value);
	}
	
	private String getDieSidesStr(int sides) {
		return Color.emphasize("d" + sides);
	}

	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public float spawnChance() {
		return 1f;
	}
}

