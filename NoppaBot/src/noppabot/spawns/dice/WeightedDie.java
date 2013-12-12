/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.INoppaBot;


public class WeightedDie extends Powerup {

	public static final int bonus = 10;

	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("A weighted die appears!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... the weighted die falls into emptiness.");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat("%s grabs the weighted die.", nick);
	}

	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		int result = roll + bonus;
		result = capResult(result);
		bot.sendChannelFormat(
			"%s's weighted die is so fairly weighted, that the roll goes very smoothly!", nick);
		bot.sendChannelFormat("%s rolls %d + %d = %d! %s", nick, roll, bonus, result,
			bot.grade(result));
		return result;
	}

	@Override
	public String getName() {
		return "Weighted Die";
	}
}