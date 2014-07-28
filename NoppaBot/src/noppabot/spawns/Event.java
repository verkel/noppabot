/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns;

import noppabot.*;
import noppabot.spawns.Spawner.SpawnInfo;


public abstract class Event implements ISpawnable {

	public static abstract class EventSpawnInfo implements SpawnInfo<Event> {

		public boolean spawnInAllEvents() {
			return true;
		}
		
		public boolean spawnInLateEvents() {
			return true;
		}
	}
	
	public abstract void run(INoppaBot bot);
	
	public String nameColored() {
		return Color.event(name());
	}
	
	@Override
	public String nameWithDetails() {
		return name();
	}
	
	@Override
	public String toStringColored() {
		return nameColored();
	}
	
	@Override
	public String toString() {
		return name();
	}
	
//	@Override
//	public boolean equals(Object obj) {
//		// This is useful for removing stuff from the spawning lists
//		return this.getClass().equals(obj.getClass());
//	}
}