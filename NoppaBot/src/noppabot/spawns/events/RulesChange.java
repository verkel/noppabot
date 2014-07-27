/*
 * Created on 17.12.2013
 * @author verkel
 */
package noppabot.spawns.events;

import java.util.*;

import noppabot.*;
import noppabot.spawns.*;
import noppabot.spawns.dice.*;
import noppabot.spawns.instants.*;


public abstract class RulesChange extends Event {
//	private static final int NUM_RULES = 4;
//	private static final int NUM_WIN_CONDITIONS = 2;
	
	public static final List<EventSpawnInfo> allInfos = Arrays.asList(
		LowestRollWins.info,
		RollClosestToTargetWins.info,
		UncappedRolls.info,
		CanDropItems.info,
		UnlimitedPowerMode.info,
		ClairvoyantMode.info,
		PeakOfEvolutionMode.info
	);
	
	@SuppressWarnings("unchecked")
	public static final Spawner<RulesChange> spawner = new Spawner(allInfos::stream);
	
	public static abstract class RulesChangeInfo extends EventSpawnInfo {
		
		@Override
		public boolean spawnInLateEvents() {
			return false;
		}
		
		@Override
		public double spawnChance() {
			return 1.0;
		}
	};
	
	@Override
	public void run(INoppaBot bot) {
		bot.sendChannelFormat("In today's Roll News, you read that the DiceRuler has issued " +
			"a temporary %s to spice things up:", nameColored());
		
		String explanation = changeRules(bot.getRules());
		bot.onRulesChanged();
		bot.sendChannel(explanation);
	}

	public abstract String changeRules(Rules rules);
	
	@Override
	public String name() {
		return "Rules Change";
	}
}

class LowestRollWins extends RulesChange {

	public static final EventSpawnInfo info = new RulesChangeInfo() {
		@Override
		public Event create() {
			return new LowestRollWins();
		}
		
		@Override
		public boolean spawnInLateEvents() {
			return false;
		}
	};
	
	@Override
	public String changeRules(Rules rules) {
		rules.winCondition.set(rules.LOWEST_ROLL);
		return rules.winCondition.get().getExplanation();
	}
}

class RollClosestToTargetWins extends RulesChange {

	public static final EventSpawnInfo info = new RulesChangeInfo() {
		@Override
		public Event create() {
			return new RollClosestToTargetWins();
		}
		
		@Override
		public boolean spawnInLateEvents() {
			return false;
		}
	};
	
	@Override
	public String changeRules(Rules rules) {
		int rollTarget = Powerups.powerupRnd.nextInt(100) + 1;
		rules.rollTarget.setValue(rollTarget);
		rules.winCondition.set(rules.ROLL_CLOSEST_TO_TARGET);
		return rules.winCondition.get().getExplanation();
	}
}

class UncappedRolls extends RulesChange {

	public static final EventSpawnInfo info = new RulesChangeInfo() {
		@Override
		public Event create() {
			return new UncappedRolls();
		}
	};
	
	@Override
	public String changeRules(Rules rules) {
		rules.cappedRolls.set(false);
		return Rules.EXPLAIN_UNCAPPED_ROLLS;
	}
}

class CanDropItems extends RulesChange {

	public static final EventSpawnInfo info = new RulesChangeInfo() {
		@Override
		public Event create() {
			return new CanDropItems();
		}
	};
	
	@Override
	public String changeRules(Rules rules) {
		rules.canDropItems.set(true);
		return Rules.EXPLAIN_CAN_DROP_ITEMS;
	}
}

class UnlimitedPowerMode extends RulesChange {
	private static final String desc = "Only items that can be infinitely upgraded are spawned.";
	private static final Spawner<BasicPowerup> spawner = Spawner.create(
		BagOfDice.info, PolishedDie.info, DicemonTrainer.info);
	
	static {
		spawner.setDescription(desc);
	}
	
	public static final EventSpawnInfo info = new RulesChangeInfo() {
		@Override
		public Event create() {
			return new UnlimitedPowerMode();
		}
	};

	private static final String explanation = String.format(
		"%s %s Also the roll cap is removed.", desc, Color.emphasize("Unlimited power mode!"));

	@Override
	public String changeRules(Rules rules) {
		rules.spawnOverride.setValue(spawner);
		rules.cappedRolls.set(false);
		return explanation;
	}
}

class ClairvoyantMode extends RulesChange {
	private static final String desc = "Only dicetellers are spawned.";
	private static final Spawner<BasicPowerup> spawner = Spawner.create(
		Diceteller.info);
	
	static {
		spawner.setDescription(desc);
	}
	
	public static final EventSpawnInfo info = new RulesChangeInfo() {
		@Override
		public Event create() {
			return new ClairvoyantMode();
		}
	};

	private static final String explanation = String.format("%s %s",
		Color.emphasize("Clairvoyant mode!"), desc);

	@Override
	public String changeRules(Rules rules) {
		rules.spawnOverride.setValue(spawner);
		return explanation;
	}
}

class PeakOfEvolutionMode extends RulesChange {
	private static final String desc = "Items spawn as upgraded.";
	private static final Spawner<BasicPowerup> spawner = Spawner.create(
		Diceteller.info);
	
	static {
		spawner.setDescription(desc);
	}
	
	public static final EventSpawnInfo info = new RulesChangeInfo() {
		@Override
		public Event create() {
			return new ClairvoyantMode();
		}
	};

	private static final String explanation = String.format("%s %s",
		Color.emphasize("Clairvoyant mode!"), desc);

	@Override
	public String changeRules(Rules rules) {
		rules.spawnOverride.setValue(spawner);
		return explanation;
	}
}