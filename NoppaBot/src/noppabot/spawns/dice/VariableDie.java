/*
 * Created on 17.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.*;
import noppabot.spawns.*;
import noppabot.spawns.events.RulesChange;


public class VariableDie extends BasicPowerup {

	private int sides;
	
	public static final BasicPowerupSpawnInfo info = new BasicPowerupSpawnInfo() {

		@Override
		public BasicPowerup create() {
			return new VariableDie();
		}
		
	};
	
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
		bot.sendChannelFormat("%s grabs the %s. It's the d%d!", 
			ownerColored, nameColored(), sides);
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the variable die breaks due to its unstable and irregular structure.");
	}

	@Override
	public int onContestRoll() {
		int result = doContestRoll(name());
		return result;
	}

	private int doContestRoll(String dieName) {
		int result = bot.getRoll(owner, sides);
		String resultStr = resultStr(result);
		result = clamp(result);
		bot.sendChannelFormat("%s rolls d%d with %s... %s! %s", 
			ownerColored, sides, dieName, resultStr, bot.grade(result));
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
		return new ChaosDie();
	}
	
	public class ChaosDie extends EvolvedPowerup {
		private String ruleChangeDescr;
		
		public ChaosDie() {
			super(VariableDie.this);
			Rules rules = bot.getRules();
			RulesChange rc = RulesChange.spawner.spawn();
			rc.changeRules(rules);
			bot.onRulesChanged();
		}
		
		@Override
		public int onContestRoll() {
			int result = doContestRoll(name());
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
