/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.INoppaBot;


public class PolishedDie extends Powerup {

	public static final int bonus = 5;

	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("A polished die appears!");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat("%s grabs the polished die.", nick);
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... the polished die fades away.");
	}

	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		int result = roll + bonus;
		result = capResult(result);
		bot.sendChannelFormat("The polished die adds a nice bonus to %s's roll.", nick);
		bot.sendChannelFormat("%s rolls %d + %d = %d! %s", nick, roll, bonus, result,
			bot.grade(result));
		return result;
	}

	@Override
	public String getName() {
		return "Polished Die";
	}
}
