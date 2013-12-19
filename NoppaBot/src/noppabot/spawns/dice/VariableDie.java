/*
 * Created on 17.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.Rules;
import noppabot.spawns.*;
import noppabot.spawns.events.RulesChange;


public class VariableDie extends BasicPowerup {

	private int sides;
	
	@Override
	public void doInitialize() {
		sides = 80 + 10 * Powerups.powerupRnd.nextInt(7);
	}
	
	@Override
	public void onSpawn() {
		bot.sendChannel("A variable die appears!");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s. It's the d%d!", 
			ownerColored, getNameColored(), sides);
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the variable die breaks due to its unstable and irregular structure.");
	}

	@Override
	public int onContestRoll(int roll) {
		int result = doContestRoll(getName());
		return result;
	}

	private int doContestRoll(String dieName) {
		int result = bot.getRollFor(owner, sides);
		String resultStr = resultStr(result);
		result = clamp(result);
		bot.sendChannelFormat("%s rolls d%d with %s... %s! %s", 
			ownerColored, sides, dieName, resultStr, bot.grade(result));
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
	public Powerup upgrade() {
		return new ChaosDie();
	}
	
	public class ChaosDie extends EvolvedPowerup {
		private String ruleChangeDescr;
		
		public ChaosDie() {
			super(VariableDie.this);
			Rules rules = bot.getRules();
			if (sides < 100) ruleChangeDescr = RulesChange.changeToLeastRollWins(rules);
			else if (sides > 100) ruleChangeDescr = RulesChange.changeToUncappedRolls(rules);
			else ruleChangeDescr = RulesChange.changeToRollClosestToTargetWins(rules);
			bot.onRulesChanged();
		}
		
		@Override
		public int onContestRoll(int roll) {
			int result = doContestRoll(getName());
			return result;
		}
		
		@Override
		public String getName() {
			return "Chaos Die";
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("It bends the rules to its liking: %s", ruleChangeDescr);
		}
	}
}
