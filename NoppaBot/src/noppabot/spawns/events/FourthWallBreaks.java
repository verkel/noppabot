/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.events;

import noppabot.INoppaBot;
import noppabot.spawns.Event;

public class FourthWallBreaks extends Event {
	@Override
	public void run(INoppaBot bot) {
		bot.sendChannelFormat("Out of nowhere, a violent wind knocks down the DiceMaster's screen! " +
			"From behind the screen, you catch a glimpse of notes written by the DM:");
		String info = bot.remainingSpawnsInfo();
		if (info.equals("")) info = "... seems that nothing of interest was in the notes!";
		bot.sendChannel(info);
	}
	
	@Override
	public String getName() {
		return "Fourth Wall Breaks";
	}
	
	@Override
	public float getSpawnChance() {
		// Compensate that it can only be the 1st to 3rd event. 
		return 2.0f;
	}
}