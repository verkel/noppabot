/*
 * Created on 17.12.2013
 * @author verkel
 */
package noppabot.spawns.events;

import noppabot.*;
import noppabot.spawns.dice.Powerups;


public class RulesChange extends Event {
	@Override
	public void run(INoppaBot bot) {
		Rules rules = bot.getRules();
		
		bot.sendChannel("In today's Roll News, you read that the DiceRuler has issued " +
			"a temporary rules change to spice things up:");
		
		int rnd = Powerups.powerupRnd.nextInt(3);
		if (rnd == 0) {
			rules.cappedRolls = false;
			bot.sendChannel("The rolls are now not limited to the 0-100 range.");
		}
		else if (rnd == 1) {
			rules.winCondition = rules.LEAST_ROLL;
			bot.sendChannel("The least roll now wins the contest!");
		}
		else if (rnd == 2) {
			int rollTarget = Powerups.powerupRnd.nextInt(100) + 1;
			rules.rollTarget = rollTarget;
			rules.winCondition = rules.ROLL_CLOSEST_TO_TARGET;
			bot.sendChannelFormat("The roll closest to number %d now wins the contest!");
		}
	}
	
	@Override
	public String getName() {
		return "Rules Change";
	}
}
