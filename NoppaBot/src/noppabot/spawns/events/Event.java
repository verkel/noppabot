/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.events;

import noppabot.INoppaBot;
import noppabot.spawns.ISpawnable;


public abstract class Event implements ISpawnable {
	public abstract void run(INoppaBot bot);
	
	public abstract String getName();

	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public float getSpawnChance() {
		return 1;
	}
}