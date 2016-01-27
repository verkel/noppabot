/*
 * Created on 17.12.2013
 * @author verkel
 */
package noppabot.spawns.events;

import java.util.*;

import noppabot.*;
import noppabot.Rules.WinCondition;
import noppabot.spawns.*;
import noppabot.spawns.dice.*;
import noppabot.spawns.instants.*;


public abstract class RulesChange extends Event {
	
	public static final List<EventSpawnInfo> allInfos = Arrays.asList(
		LowestRollWins.info,
		RollClosestToTargetWins.info,
		UncappedRolls.info,
		CanDropItems.info,
		UnlimitedPowerMode.info,
		ClairvoyantMode.info,
		PeakOfEvolutionMode.info,
		DiceFusionMode.info,
		PokerNightMode.info
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
			return 0.5;
		}
	};
	
	@Override
	public void run(INoppaBot bot) {
		bot.sendChannelFormat("In today's Roll News, you read that the DiceRuler has issued " +
			"a temporary %s to spice things up:", nameColored());
		bot.sendChannel(explanation(bot.getRules()));
		
		changeRules(bot.getRules());
	}

	public abstract void changeRules(Rules rules);
	public abstract String explanation(Rules rules);
	
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
		public double spawnChance() {
			return 0.25;
		}
	};
	
	@Override
	public String explanation(Rules rules) {
		return rules.LOWEST_ROLL.getExplanation();
	}
	
	@Override
	public void changeRules(Rules rules) {
		rules.winCondition.set(rules.LOWEST_ROLL);
	}
}

class RollClosestToTargetWins extends RulesChange {

	public static final EventSpawnInfo info = new RulesChangeInfo() {
		@Override
		public Event create() {
			return new RollClosestToTargetWins();
		}
		
		@Override
		public double spawnChance() {
			return 0.25;
		}
	};
	
	private WinCondition winCondition;
	private WinCondition getWinCondition(Rules rules) {
		if (winCondition == null) {
			int rollTarget = Powerups.powerupRnd.nextInt(100) + 1;
			winCondition = rules.winConditionRollClosestToTarget(rollTarget);
		}
		return winCondition;
	}
	
	@Override
	public void changeRules(Rules rules) {
		rules.winCondition.set(getWinCondition(rules));
	}

	@Override
	public String explanation(Rules rules) {
		return getWinCondition(rules).getExplanation();
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
	public void changeRules(Rules rules) {
		rules.cappedRolls.set(false);
	}

	@Override
	public String explanation(Rules rules) {
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
	public void changeRules(Rules rules) {
		rules.canDropItems.set(true);
	}

	@Override
	public String explanation(Rules rules) {
		return Rules.EXPLAIN_CAN_DROP_ITEMS;
	}
}

class UnlimitedPowerMode extends RulesChange {
	private static final String desc = "Only items that can be infinitely upgraded are spawned.";
	private static final Spawner<BasicPowerup> itemSpawner = Spawner.create(
		BagOfDice.info, PolishedDie.info, DicemonTrainer.info.withChance(1.0));
	
	static {
		itemSpawner.setDescription(desc);
	}
	
	public static final EventSpawnInfo info = new RulesChangeInfo() {
		@Override
		public Event create() {
			return new UnlimitedPowerMode();
		}
	};

	@Override
	public void changeRules(Rules rules) {
		rules.spawnOverride.setValue(itemSpawner);
		rules.cappedRolls.set(false);
	}

	@Override
	public String explanation(Rules rules) {
		return String.format("%s %s Also the roll cap is removed.",
			Color.rulesMode("Unlimited power mode!"), desc);
	}
}

class ClairvoyantMode extends RulesChange {
	private static final String desc = "Only dicetellers are spawned.";
	private static final Spawner<BasicPowerup> itemSpawner = Spawner.create(
		Diceteller.info);
	
	static {
		itemSpawner.setDescription(desc);
	}
	
	public static final EventSpawnInfo info = new RulesChangeInfo() {
		@Override
		public Event create() {
			return new ClairvoyantMode();
		}
	};

	@Override
	public void changeRules(Rules rules) {
		rules.spawnOverride.setValue(itemSpawner);
	}

	@Override
	public String explanation(Rules rules) {
		return String.format("%s %s", Color.rulesMode("Clairvoyant mode!"), desc);
	}
}

class PeakOfEvolutionMode extends RulesChange {
	private static final String desc = "Items spawn as upgraded.";
	
	public static final EventSpawnInfo info = new RulesChangeInfo() {
		@Override
		public Event create() {
			return new PeakOfEvolutionMode();
		}
	};
	
	@Override
	public void changeRules(Rules rules) {
		rules.upgradedSpawns.set(true);
	}

	@Override
	public String explanation(Rules rules) {
		return String.format("%s %s", Color.rulesMode("Peak of evolution mode!"), desc);
	}
}

class DiceFusionMode extends RulesChange {
	public static final EventSpawnInfo info = new RulesChangeInfo() {
		@Override
		public Event create() {
			return new DiceFusionMode();
		}
	};
	
	@Override
	public void changeRules(Rules rules) {
		rules.diceFusion.set(true);
	}

	@Override
	public String explanation(Rules rules) {
		return String.format("%s %s", Color.rulesMode("Dice fusion mode!"), Rules.EXPLAIN_DICE_FUSION);
	}
}

class PokerNightMode extends RulesChange {
	public static final EventSpawnInfo info = new RulesChangeInfo() {
		@Override
		public Event create() {
			return new PokerNightMode();
		}
		
		@Override
		public double spawnChance() { // Haven't seen these for a while :(
			return 2.0;
		}
	};
	
	@Override
	public void changeRules(Rules rules) {
		rules.spawnOverride.setValue(Rules.POKER_NIGHT_SPAWNER);
	}

	@Override
	public String explanation(Rules rules) {
		return String.format("%s %s", Color.rulesMode("Poker night mode!"), Rules.EXPLAIN_POKER_NIGHT_SPAWNER);
	}
}
