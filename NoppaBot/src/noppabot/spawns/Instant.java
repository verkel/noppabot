/*
 * Created on 19.12.2013
 * @author verkel
 */
package noppabot.spawns;

import noppabot.ColorStr;


public abstract class Instant extends BasicPowerup {
	
	@Override
	public boolean isCarried() {
		return false;
	}
	
	@Override
	public String nameColored() {
		return ColorStr.instant(name());
	}
	
	@Override
	public int sides() {
		return -1;
	}
}
