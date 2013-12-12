/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import java.util.*;
import java.util.Map.Entry;

import noppabot.INoppaBot;


public class MasterDie extends Powerup {

	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("The MASTER DIE appears!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... nobody took the MASTER DIE?! Maybe you should learn some dice appraisal skills at the certified rolling professional.");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat(
			"%s wrestles for the MASTER DIE with the other contestants and barely comes on top. Surely, the MASTER DIE must be worth all the trouble!",
			nick);
		
		bot.insertApprenticeDice();
	}

	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		int uncappedResult = bot.getRollFor(nick, 200);
		int result = capResult(uncappedResult);
		bot.sendChannelFormat("%s rolls d200 with the MASTER DIE...", nick);
		if (uncappedResult > 100) {
			bot.sendChannelFormat("%s rolls %d (= 100)! %s", nick, uncappedResult, bot.grade(result));
		}
		else bot.sendDefaultContestRollMessage(nick, result);
		rollUnusedApprenticeDies(bot, result);
		return result;
	}
	
	private void rollUnusedApprenticeDies(INoppaBot bot, int roll) {
		Map<String, Powerup> powerups = bot.getPowerups();
		for (Entry<String, Powerup> entry : powerups.entrySet()) {
			String owner = entry.getKey();
			Powerup powerup = entry.getValue();
			if (powerup instanceof ApprenticeDie && !bot.participated(owner)) {
				bot.sendChannelFormat("%s's apprentice die observes and then rolls %d.", owner, roll);
				// Don't use participate so we wont end the contest on overtime
				// and forget the master die's roll
				bot.getRolls().put(owner, roll);
			}
		}
	}

	@Override
	public String getName() {
		return "MASTER DIE";
	}
	
	@Override
	public float getSpawnChance() {
		return 0.25f;
	}
}