/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.INoppaBot;


public class LuckyDie extends Powerup {

	public static final int bonus = 25;

	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("A lucky die appears!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... the lucky die rolls away.");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat(
			"%s grabs the lucky die and it wishes good luck for tonight's roll.", nick);
	}

	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		if (String.valueOf(roll).contains("7")) {
			int result = roll + bonus;
			result = capResult(result);
			bot.sendChannelFormat(
				"%s rolls %d! The lucky die likes sevens in numbers, so it tinkers with your roll, making it %d + %d = %d. Lucky!",
				nick, roll, roll, bonus, result);
			return result;
		}
		else {
			bot.sendChannelFormat(
				"%s rolls %d! The lucky die doesn't seem to like this number, though.", nick, roll);
			return roll;
		}
	}

	@Override
	public String getName() {
		return "Lucky Die";
	}
}