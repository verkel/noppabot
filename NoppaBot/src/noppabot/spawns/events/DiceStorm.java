/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.events;

import noppabot.INoppaBot;
import noppabot.spawns.*;

public class DiceStorm extends Event {
	
	public static final EventSpawnInfo info = new EventSpawnInfo() {

		@Override
		public Event create() {
			return new DiceStorm();
		}
		
	};
	
	@Override
	public void run(INoppaBot bot) {
		bot.sendChannelFormat("Suddenly, a %s breaks out! It's now raining dice!", nameColored());
		
		int count = 3 + Powerups.powerupRnd.nextInt(2);
		for (int i = 0; i < count; i++) {
			bot.scheduleRandomSpawn(null, Powerups.diceStormPowerups, null);
		}
	}
	
	@Override
	public String name() {
		return "Dice Storm";
	}
}