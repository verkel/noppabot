/*
 * Created on 17.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.*;
import noppabot.spawns.events.RulesChange;


public class VariableDie extends Powerup {

	private int sides;
	
	@Override
	public void initialize(INoppaBot bot) {
		sides = 80 + 10 * Powerups.powerupRnd.nextInt(7);
	}
	
	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("A variable die appears!");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat("%s grabs the variable die. It's the d%d!", nick, sides);
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... the variable die breaks due to its unstable and irregular structure.");
	}

	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		int result = doContestRoll(bot, nick, getName());
		return result;
	}

	private int doContestRoll(INoppaBot bot, String nick, String dieName) {
		int result = bot.getRollFor(nick, sides);
		String resultStr = rollToString(bot, result);
		result = clamp(bot, result);
		bot.sendChannelFormat("%s rolls d%d with %s... %s! %s", 
			nick, sides, dieName, resultStr, bot.grade(result));
		return result;
	}

	@Override
	public String getName() {
		return "Variable Die";
	}
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade(INoppaBot bot) {
		return new ChaosDie(bot);
	}
	
	public class ChaosDie extends Powerup {
		private String ruleChangeDescr;
		
		public ChaosDie(INoppaBot bot) {
			Rules rules = bot.getRules();
			if (sides < 100) ruleChangeDescr = RulesChange.changeToLeastRollWins(rules);
			else if (sides > 100) ruleChangeDescr = RulesChange.changeToUncappedRolls(rules);
			else ruleChangeDescr = RulesChange.changeToRollClosestToTargetWins(rules);
		}
		
		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			int result = doContestRoll(bot, nick, getName());
			return result;
		}
		
		@Override
		public String getName() {
			return "Chaos Die";
		}
		
		@Override
		public String getUpgradeDescription(INoppaBot bot, String nick) {
			return String.format("It bends the rules to its liking: %s", ruleChangeDescr);
		}
	}
}
