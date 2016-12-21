/*
 * Created on 24.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import java.time.*;

import noppabot.spawns.*;
import noppabot.spawns.Instant;


public class Present extends Instant {

	public static final double CHRISTMAS_SPAWN_CHANCE = 10.00;
	
	public static final PresentSpawnInfo info = new PresentSpawnInfo();
	
	public static class PresentSpawnInfo extends InstantSpawnInfo {

		@Override
		public Instant create() {
			return new Present();
		}
		
		@Override
		public double spawnChance() {
			if (isChristmasTime()) return CHRISTMAS_SPAWN_CHANCE;
			else return 0.33;
		}
		
		private boolean isChristmasTime() {
			LocalDate now = currentDate();
			return now.isAfter(decemberDay(20)) && now.isBefore(decemberDay(25));
		}
		
		public LocalDate currentDate() {
			return LocalDate.now();
		}
		
		private LocalDate decemberDay(int dayOfMonth) {
			return LocalDate.of(Year.now().getValue(), Month.DECEMBER, dayOfMonth);
		}
	};
	
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
			bot.scheduleRandomSpawn(null, Powerups.presentPowerups, null);
		}
	}

	@Override
	public String name() {
		return "Present";
	}
}
