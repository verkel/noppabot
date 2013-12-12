/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.INoppaBot;


public class ExtremeDie extends Powerup {

	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("The extreme die appears!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... the extreme die gets bored sitting still and takes off.");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat(
			"%s grabs the extreme die! You discuss about various subcultures popular with radical dice.",
			nick);
	}

	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		if (roll == 100) {
			bot.sendChannelFormat("%s rolls %d with the extreme die! That's the most extreme roll and the die is ecstatic!", nick, roll); 
			return roll;
		}
		else if (roll > 10 && roll < 90) {
			bot.sendChannelFormat("%s rolls %d with the extreme die! This number is quite ordinary, says the die.", nick, roll);
			return roll;
		}
		else {
			bot.sendChannelFormat("%s rolls %d with the extreme die! This number is very extremal! says the die.", nick, roll);
			bot.sendChannelFormat("The extreme die rewards %s with the most extreme roll, 100!", nick);
			return 100;
		}
	}

	@Override
	public String getName() {
		return "Extreme Die";
	}
}
