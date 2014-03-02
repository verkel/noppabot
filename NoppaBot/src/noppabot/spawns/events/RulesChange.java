/*
 * Created on 17.12.2013
 * @author verkel
 */
package noppabot.spawns.events;

import java.util.Calendar;

import noppabot.*;
import noppabot.spawns.*;


public class RulesChange extends Event {
	private static final int NUM_RULES = 4;
	private static final int NUM_WIN_CONDITIONS = 2;
	
	@Override
	public void run(INoppaBot bot) {
		bot.sendChannelFormat("In today's Roll News, you read that the DiceRuler has issued " +
			"a temporary %s to spice things up:", nameColored());
		
		String descr = doRandomRulesChange(bot);
		bot.sendChannel(descr);
	}

	public static String doRandomRulesChange(INoppaBot bot) {
		Rules rules = bot.getRules();
		int rnd = getRnd();
		String explanation;
		if (rnd == 0) {
			explanation = changeToLowestRollWins(rules);
		}
		else if (rnd == 1) {
			explanation = changeToRollClosestToTargetWins(rules);
		}
		else if (rnd == 2) {
			explanation = changeToUncappedRolls(rules);
		}
		else if (rnd == 3) {
			explanation = changeToCanDropItems(rules);
		}
		else {
			throw new IllegalStateException();
		}
		
		bot.onRulesChanged();
		return explanation;
	}
	
	private static int getRnd() {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if (hour < 16) {
			return Powerups.powerupRnd.nextInt(NUM_RULES);
		}
		else {
			return NUM_WIN_CONDITIONS + Powerups.powerupRnd.nextInt(NUM_RULES - NUM_WIN_CONDITIONS);
		}
	}

	public static String changeToRollClosestToTargetWins(Rules rules) {
		int rollTarget = Powerups.powerupRnd.nextInt(100) + 1;
		rules.rollTarget = rollTarget;
		rules.winCondition = rules.ROLL_CLOSEST_TO_TARGET;
		return rules.winCondition.getExplanation();
	}

	public static String changeToLowestRollWins(Rules rules) {
		rules.winCondition = rules.LOWEST_ROLL;
		return rules.winCondition.getExplanation();
	}

	public static String changeToUncappedRolls(Rules rules) {
		rules.cappedRolls = false;
		return Rules.EXPLAIN_UNCAPPED_ROLLS;
	}
	
	public static String changeToCanDropItems(Rules rules) {
		rules.canDropItems = true;
		return Rules.EXPLAIN_CAN_DROP_ITEMS;
	}
	
	@Override
	public String name() {
		return "Rules Change";
	}
	
	@Override
	public float spawnChance() {
		return 1.0f;
	}
}
