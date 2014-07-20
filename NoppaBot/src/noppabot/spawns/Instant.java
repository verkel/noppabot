/*
 * Created on 19.12.2013
 * @author verkel
 */
package noppabot.spawns;

import noppabot.Color;


public abstract class Instant extends BasicPowerup {

	public static abstract class InstantSpawnInfo extends BasicPowerupSpawnInfo {

		@Override
		public boolean spawnInFirstPowerups() {
			return false;
		}

		@Override
		public boolean spawnInDiceBrosPowerups() {
			return false;
		}
	}

	@Override
	public boolean isCarried() {
		return false;
	}
	
	@Override
	public boolean isDestroyedAfterPickup() {
		return true;
	}
	
	@Override
	public String nameColored() {
		return Color.instant(name());
	}
	
	@Override
	public int sides() {
		return -1;
	}
}
