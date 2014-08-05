/*
 * Created on 17.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.*;
import noppabot.spawns.*;
import noppabot.spawns.events.RulesChange;


public class VariableDie extends BasicDie {

	private int sides;
	
	public static final BasicPowerupSpawnInfo info = new BasicPowerupSpawnInfo() {

		@Override
		public BasicPowerup create() {
			return new VariableDie();
		}
		
	};
	
	@Override
	public SpawnInfo<?> spawnInfo() {
		return info;
	}
	
	@Override
	public void onInitialize() {
		sides = 80 + 10 * Powerups.powerupRnd.nextInt(7);
	}
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", nameColored());
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s. It's the d%s!", 
			ownerColored, nameColored(), sides);
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the variable die breaks due to its unstable and irregular structure.");
	}

	@Override
	public DiceRoll onContestRoll() {
		DiceRoll result = doContestRoll(name());
		return result;
	}

	private DiceRoll doContestRoll(String dieName) {
		DiceRoll result = bot.getRoll(owner, sides);
		bot.sendChannelFormat("%s rolls d%s with %s... %s! %s", 
			ownerColored, sides, dieName, resultStr(result), bot.grade(result));
		return result;
	}

	@Override
	public String name() {
		return "Variable Die";
	}

	@Override
	public String details() {
		return "d" + sides;
	}
	
	@Override
	public int sides() {
		return sides;
	}
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade() {
		// Don't allow chaos die to be upgraded automatically by the
		// "items spawn as upgraded" rules change
		if (owner == null) return this;
		return new ChaosDie();
	}
	
	public class ChaosDie extends EvolvedDie {
		private String ruleChangeDescr;
		
		public ChaosDie() {
			super(VariableDie.this);
			Rules rules = bot.getRules();
			RulesChange rc = RulesChange.spawner.spawn();
			ruleChangeDescr = rc.explanation(rules);
			rc.changeRules(rules);
		}
		
		@Override
		public DiceRoll onContestRoll() {
			DiceRoll result = doContestRoll(name());
			return result;
		}
		
		@Override
		public String name() {
			return "Chaos Die";
		}

		@Override
		public String getUpgradeDescription() {
			return String.format("It %s to its liking: %s", Color.event("bends the rules"), ruleChangeDescr);
		}
	}
}
