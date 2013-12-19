/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.events;

import noppabot.INoppaBot;
import noppabot.spawns.*;

public class DiceStorm extends Event {
	@Override
	public void run(INoppaBot bot) {
		bot.sendChannelFormat("Suddenly, a %s breaks out! It's now raining dice!", getNameColored());
		
		int count = 3 + Powerups.powerupRnd.nextInt(2);
		for (int i = 0; i < count; i++) {
			bot.scheduleRandomSpawn(null, Powerups.diceStormPowerups, null);
		}
	}
	
	@Override
	public String getName() {
		return "Dice Storm";
	}
}