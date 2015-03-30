/*
 * Created on 31.3.2015
 * @author verkel
 */
package noppabot.spawns.events;

import noppabot.*;
import noppabot.spawns.Event;


public class RollPrediction extends Event {
	public static final EventSpawnInfo info = new EventSpawnInfo() {

		@Override
		public Event create() {
			return new RollPrediction();
		}
		
		@Override
		public boolean spawnInLateEvents() {
			return false;
		}
	};
	
	private static Fortunes fortunes = new Fortunes();
	
	@Override
	public void run(INoppaBot bot) {
		bot.sendChannelFormat("It's the Diceteller's %s in today's Roll News!", Color.event("roll'o'scope"));
		String fortune = "\"" + fortunes.random();
		for (String line : fortune.split("\\n")) {
			bot.sendChannel(line);
		}
		String prediction = "[roll prediction]";
		bot.sendChannelFormat("Also, %s\"", Color.rulesMode(prediction));
	}
	
	@Override
	public String name() {
		return "Roll Prediction";
	}
}
