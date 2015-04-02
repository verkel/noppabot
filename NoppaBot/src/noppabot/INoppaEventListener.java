/*
 * Created on 10.12.2014
 * @author verkel
 */
package noppabot;

import java.util.Set;

import noppabot.spawns.Powerup;


public interface INoppaEventListener {
	void powerupSpawned(Powerup<?> powerup);
	void rollPeriodStarted();
	void rollPeriodEnded();
	void settleTieStarted(Set<String> tiebreakers);
}
