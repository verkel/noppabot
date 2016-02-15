/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.events;

import noppabot.INoppaBot;
import noppabot.spawns.Event;

public class FourthWallBreaks extends Event {
	public static final EventSpawnInfo info = new EventSpawnInfo() {

		@Override
		public Event create() {
			return new FourthWallBreaks();
		}
		
		@Override
		public double spawnChance() {
			// Compensate that these stop spawning after 16:00
			return 2.0;
		}
		
		@Override
		public boolean spawnInLateEvents() {
			return false;
		}
	};
	
	@Override
	public void run(INoppaBot bot) {
		bot.sendChannelFormat("Out of nowhere, a violent wind knocks down the DiceMaster's screen! " +
			"From behind the screen, you catch a glimpse of notes written by the DM:");
		String info = bot.remainingSpawnsInfo();
		if (info.equals("")) info = "... seems that nothing of interest was in the notes!";
		bot.sendChannel(info);
	}
	
	@Override
	public String name() {
		return "Fourth Wall Breaks";
	}
}