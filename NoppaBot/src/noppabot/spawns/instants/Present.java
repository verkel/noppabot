/*
 * Created on 24.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import noppabot.spawns.*;


public class Present extends Instant {
	
//	public static Spawner<BasicPowerup> spawner = new Spawner<BasicPowerup>(
//		Arrays.<BasicPowerup>asList(new Present()));
	
	public static final BasicPowerupSpawnInfo info = dontSpawnInfo;
	
	@Override
	public SpawnInfo<?> spawnInfo() {
		return info;
	}
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", nameColored());
	}

	@Override
	public void onExpire() {
		bot.sendChannelFormat("... the present expires.");
	}

	@Override
	public boolean canPickUp(String nick, boolean verbose) {
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
	public String name() {
		return "Present";
	}
}
