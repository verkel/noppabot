/*
 * Created on 17.12.2013
 * @author verkel
 */
package noppabot.spawns.events;

import noppabot.*;
import noppabot.spawns.*;


public class RulesChange extends Event {
	@Override
	public void run(INoppaBot bot) {
		bot.sendChannelFormat("In today's Roll News, you read that the DiceRuler has issued " +
			"a temporary %s to spice things up:", getNameColored());
		
		String descr = doRandomRulesChange(bot);
		bot.sendChannel(descr);
	}

	public static String doRandomRulesChange(INoppaBot bot) {
		Rules rules = bot.getRules();
		int rnd = Powerups.powerupRnd.nextInt(3);
		String explanation;
		if (rnd == 0) {
			explanation = changeToUncappedRolls(rules);
		}
		else if (rnd == 1) {
			explanation = changeToLowestRollWins(rules);
		}
		else if (rnd == 2) {
			explanation = changeToRollClosestToTargetWins(rules);
		}
		else {
			throw new IllegalStateException();
		}
		
		bot.onRulesChanged();
		return explanation;
	}

	public static String changeToRollClosestToTargetWins(Rules rules) {
		int rollTarget = Powerups.powerupRnd.nextInt(100) + 1;
		rules.rollTarget = rollTarget;
		rules.winCondition = rules.ROLL_CLOSEST_TO_TARGET;
		return String.format("The roll closest to number %d now wins the contest!", rollTarget);
	}

	public static String changeToLowestRollWins(Rules rules) {
		rules.winCondition = rules.LOWEST_ROLL;
		return "The lowest roll now wins the contest!";
	}

	public static String changeToUncappedRolls(Rules rules) {
		rules.cappedRolls = false;
		return "The rolls are now not limited to the 0-100 range.";
	}
	
	@Override
	public String getName() {
		return "Rules Change";
	}
}
