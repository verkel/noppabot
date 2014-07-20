/*
 * Created on 30.1.2014
 * @author verkel
 */
package noppabot.spawns.events;

import noppabot.*;
import noppabot.spawns.Event;


public class FavorRefresh extends Event {
	
	public static final EventSpawnInfo info = new EventSpawnInfo() {

		@Override
		public Event create() {
			return new FavorRefresh();
		}
	};
	
	@Override
	public void run(INoppaBot bot) {
		bot.sendChannelFormat("Hey guys, I'm in a good mood! I'll %s you've used!", Color.event("Refresh Favors"));
		bot.clearFavorsUsed();
	}
	
	@Override
	public String name() {
		return "Favor Refresh";
	}
}
