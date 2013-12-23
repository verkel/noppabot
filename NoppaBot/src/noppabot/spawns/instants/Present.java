/*
 * Created on 24.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import java.util.Arrays;

import noppabot.spawns.*;
import noppabot.spawns.Spawner.LastSpawn;


public class Present extends Instant {
	
	public static Spawner<Present> spawner = new Spawner<Present>(Arrays.asList(new Present()), new LastSpawn());
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", getNameColored());
	}

	@Override
	public void onExpire() {
		bot.sendChannelFormat("... the present expires.");
	}

	@Override
	public boolean canPickUp(String nick) {
		return true;
	}
	
	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s opens the present! There's a bunch of dice inside:", ownerColored);
		
		int count = 3 + Powerups.powerupRnd.nextInt(2);
		for (int i = 0; i < count; i++) {
			bot.scheduleRandomSpawn(null, Powerups.diceStormPowerups, null);
		}
	}

	@Override
	public String getName() {
		return "Present";
	}
}
