/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns;

import noppabot.*;


public abstract class Event implements ISpawnable {
	public abstract void run(INoppaBot bot);
	
	public abstract String getName();

	public String getNameColored() {
		return ColorStr.event(getName());
	}
	
	@Override
	public String toStringColored() {
		return getNameColored();
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public float spawnChance() {
		return 1;
	}
	
	@Override
	public boolean equals(Object obj) {
		// This is useful for removing stuff from the spawning lists
		return this.getClass().equals(obj.getClass());
	}
}