/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.INoppaBot;

public class FastDie extends Powerup {

	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("The fast die appears!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... the fast die was too fast for you.");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat(
			"%s quickly grabs the fast die! It asks you if you can throw it even faster when the time comes.",
			nick);
	}

	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		int seconds = bot.getSecondsAfterMidnight();
		int bonus = Math.max(0, 30 - seconds);
		int result = roll + bonus;
		result = capResult(result);
		bot.sendChannelFormat("%s waited %d seconds before rolling. The fast die awards +%d speed bonus!", 
			nick, seconds, bonus);
		bot.sendChannelFormat("%s rolls %d + %d = %d! %s", nick, roll, bonus, result, bot.grade(result));
		return result;
	}

	@Override
	public String getName() {
		return "Fast Die";
	}
	
	
	@Override
	public float getSpawnChance() {
		return 0.50f;
	}
}