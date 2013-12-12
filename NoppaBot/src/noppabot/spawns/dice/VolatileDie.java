/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.INoppaBot;


public class VolatileDie extends Powerup {

	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("The volatile die appears!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... the volatile die grows unstable and explodes.");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat("%s grabs the volatile die! It glows with an aura of uncertainty.",
			nick);
	}

	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		bot.sendChannelFormat("%s rolls with the volatile die. %d! %s", nick, roll, bot.grade(roll));
		
		while (getRerollRnd() > roll) {
			roll = bot.getRollFor(nick, 100);
			bot.sendChannelFormat("%s's volatile die keeps on rolling! %d! %s", nick, roll, bot.grade(roll));
		}
		
		return roll;
	}
	
	private int getRerollRnd() {
		return Powerups.powerupRnd.nextInt(100)+1;
	}

	@Override
	public String getName() {
		return "Volatile Die";
	}
}