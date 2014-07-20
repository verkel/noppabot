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
		CanDropItems.info
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
		
//		String explanation = doRandomRulesChange(bot);
		String explanation = changeRules(bot.getRules());
		bot.onRulesChanged();
		bot.sendChannel(explanation);
	}

//	public static String doRandomRulesChange(INoppaBot bot) {
//		Rules rules = bot.getRules();
////		int rnd = getRnd();
////		String explanation;
////		if (rnd == 0) {
////			explanation = changeToLowestRollWins(rules);
////		}
////		else if (rnd == 1) {
////			explanation = changeToRollClosestToTargetWins(rules);
////		}
////		else if (rnd == 2) {
////			explanation = changeToUncappedRolls(rules);
////		}
////		else if (rnd == 3) {
////			explanation = changeToCanDropItems(rules);
////		}
////		else {
////			throw new IllegalStateException();
////		}
//		
//		String explanation = changeRules(rules);
//		
//		bot.onRulesChanged();
//		return explanation;
//	}
	
	public abstract String changeRules(Rules rules);
	
//	private static int getRnd() {
//		Calendar cal = Calendar.getInstance();
//		int hour = cal.get(Calendar.HOUR_OF_DAY);
//		if (hour < 16) {
//			return Powerups.powerupRnd.nextInt(NUM_RULES);
//		}
//		else {
//			return NUM_WIN_CONDITIONS + Powerups.powerupRnd.nextInt(NUM_RULES - NUM_WIN_CONDITIONS);
//		}
//	}

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
		rules.winCondition = rules.LOWEST_ROLL;
		return rules.winCondition.getExplanation();
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
		rules.rollTarget = rollTarget;
		rules.winCondition = rules.ROLL_CLOSEST_TO_TARGET;
		return rules.winCondition.getExplanation();
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
		rules.cappedRolls = false;
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
		rules.canDropItems = true;
		return Rules.EXPLAIN_CAN_DROP_ITEMS;
	}
}

class UnlimitedPowerMode extends RulesChange {
	private static final Spawner<BasicPowerup> spawner = Spawner.create(
		BagOfDice.info, PolishedDie.info, DicemonTrainer.info);
	
	public static final EventSpawnInfo info = new RulesChangeInfo() {
		@Override
		public Event create() {
			return new UnlimitedPowerMode();
		}
	};

	private static final String explanation = String.format(
		"%s Only items that can be infinitely upgraded are spawned. Also the roll cap is removed.",
		Color.emphasize("Unlimited power mode!"));

	@Override
	public String changeRules(Rules rules) {
		rules.spawnOverride = Optional.of(spawner);
		rules.cappedRolls = false;
		return explanation;
	}
}

class ClairvoyantMode extends RulesChange {
	private static final Spawner<BasicPowerup> spawner = Spawner.create(
		Diceteller.info);
	
	public static final EventSpawnInfo info = new RulesChangeInfo() {
		@Override
		public Event create() {
			return new ClairvoyantMode();
		}
	};

	private static final String explanation = String.format(
		"%s Only dicetellers are spawned.",
		Color.emphasize("Clairvoyant mode!"));

	@Override
	public String changeRules(Rules rules) {
		rules.spawnOverride = Optional.of(spawner);
		return explanation;
	}
}

class PeakOfEvolutionMode extends RulesChange {
	private static final Spawner<BasicPowerup> spawner = Spawner.create(
		Diceteller.info);
	
	public static final EventSpawnInfo info = new RulesChangeInfo() {
		@Override
		public Event create() {
			return new ClairvoyantMode();
		}
	};

	private static final String explanation = String.format(
		"%s Only dicetellers are spawned.",
		Color.emphasize("Clairvoyant mode!"));

	@Override
	public String changeRules(Rules rules) {
		rules.spawnOverride = Optional.of(spawner);
		return explanation;
	}
}