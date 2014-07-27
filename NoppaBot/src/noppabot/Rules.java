/*
 * Created on 17.12.2013
 * @author verkel
 */
package noppabot;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;

import noppabot.spawns.*;
import noppabot.spawns.dice.PokerHand;
import noppabot.spawns.instants.*;

import org.jibble.pircbot.Colors;


public class Rules {
	public static abstract class WinCondition { // implements Comparator<Entry<String, Integer>> {
		public abstract int assignScore(int roll);
		
		public final Comparator<Entry<String, Integer>> rollEntryComparator = 
			new Comparator<Map.Entry<String,Integer>>() {
			
			@Override
			public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
				int cmp = WinCondition.this.compare(e1.getValue(), e2.getValue());
				if (cmp != 0) return cmp;
				return e1.getKey().compareTo(e2.getKey());
			}
		};
		
		public final Comparator<Integer> rollComparator = new Comparator<Integer>() {
			
			@Override
			public int compare(Integer r1, Integer r2) {
				return WinCondition.this.compare(r1, r2);
			}
		};
		
		protected int compare(int roll1, int roll2) {
			return -MathUtils.compare(assignScore(roll1), assignScore(roll2));
		}
		
		/**
		 * Here we should notify about non-standard win conditions
		 */
		public void onRollPeriodStart(INoppaBot bot) {
		}
		
		/**
		 * Here we should tell how the roll is graded
		 */
		public void onContestRoll(INoppaBot bot, String nick, int roll) {
		}
		
		public abstract String getExplanation();
	}
	
	public final WinCondition HIGHEST_ROLL = new WinCondition() {
		
		@Override
		public int assignScore(int roll) {
			return roll;
		}

		@Override
		public String getExplanation() {
			return "The highest roll wins the contest!";
		}
	};
	
	public final WinCondition LOWEST_ROLL = new WinCondition() {
		@Override
		public void onRollPeriodStart(INoppaBot bot) {
			bot.sendChannel("Tonight we are looking for the lowest roll!");
		}
		
		@Override
		public int assignScore(int roll) {
			return 100 - roll;
		}

		@Override
		public String getExplanation() {
			return "The lowest roll wins the contest!";
		}
	};
	
	public final WinCondition POKER_HAND_VALUE = new WinCondition() {
		@Override
		public void onRollPeriodStart(INoppaBot bot) {
			bot.sendChannel("Players, reveal your hands!");
		}
		
		@Override
		public int assignScore(int roll) {
			return 100 - roll;
		}

		@Override
		public String getExplanation() {
			return "The best poker hand wins the contest!";
		}
	};
	
	public final RollClosestToTarget winConditionRollClosestToTarget(int rollTarget) {
		return new RollClosestToTarget(rollTarget);
	}
	
	public static class RollClosestToTarget extends WinCondition {
		private int rollTarget;
		
		public RollClosestToTarget(int rollTarget) {
			this.rollTarget = rollTarget;
		}

		@Override
		public void onRollPeriodStart(INoppaBot bot) {
			bot.sendChannelFormat("Tonight we are looking for the roll which is closest " +
				"to number %d!", rollTarget);
		}
		
		@Override
		public int assignScore(int roll) {
			return 100 - Math.abs(rollTarget - roll);
		}
		
		@Override
		public void onContestRoll(INoppaBot bot, String nick, int roll) {
			int dist = Math.abs(rollTarget - roll);
			if (dist > 0) bot.sendChannelFormat("%s's roll is %d points off the target", nick, dist);
			else bot.sendChannelFormat("%s's roll hit the target!", nick);
		}
		
		@Override
		public String getExplanation() {
			return String.format("The roll closest to number %d wins the contest!", rollTarget);
		}

		public int getRollTarget() {
			return rollTarget;
		}
	};
	
	/**
	 * Rolls have a 0..100 point cap
	 */
	public final Property<Boolean> cappedRolls = Property.of(true);
	
	/**
	 * How is the winner determined
	 */
	public final Property<WinCondition> winCondition = Property.of(HIGHEST_ROLL);
	
	/**
	 * Items can be dropped to ground at any time
	 */
	public final Property<Boolean> canDropItems = Property.of(false);

	/**
	 * Item spawner to be used instead of the regular spawner
	 */
	public OptionalProperty<Spawner<BasicPowerup>> spawnOverride = OptionalProperty.create();

	/**
	 * Whether items spawn as upgraded
	 */
	public Property<Boolean> upgradedSpawns = Property.of(false);
	
	/**
	 * Can upgrade dice by grabbing another one of the same type
	 */
	public Property<Boolean> diceFusion = Property.of(false);
	
	private List<Property<?>> all = Arrays.asList(cappedRolls, winCondition,
		canDropItems, spawnOverride, upgradedSpawns, diceFusion);

	private INoppaBot bot;

	public Rules(INoppaBot bot) {
		this.bot = bot;
		doReset();
	}
	
	public void onRollPeriodStart() {
		if (cappedRolls.isChanged()) {
			bot.sendChannel("Remember, the rolls are not restricted to the 0-100 range now.");
		}
		if (winCondition.isChanged()) {
			winCondition.get().onRollPeriodStart(bot);
		}
	}
	
	public ISpawnable getRandomPowerupOrEvent(INoppaBot bot, Spawner<BasicPowerup> allowedPowerups, Spawner<Event> allowedEvents) {
		if (spawnOverride.isPresent()) return spawnOverride.getValue().spawn();
		else return Powerups.getRandomPowerupOrEvent(bot, allowedPowerups, allowedEvents);
	}
	
	public Spawner<BasicPowerup> getPowerupsSpawner(Spawner<BasicPowerup> proposed) {
		return spawnOverride.orElse(proposed);
	}
	
	/**
	 * Reset the rules
	 * 
	 * @return if the rules changed
	 */
	public boolean reset() {
		boolean changed = isChanged();
		doReset();
		return changed;
	}
	
	public boolean isPokerNight() {
		return spawnOverride.getValue() == POKER_NIGHT_SPAWNER;
	}
	
	private void doReset() {
		all.forEach(rule -> rule.reset());
	}
	
	private boolean isChanged() {
		return all.stream().anyMatch(rule -> rule.isChanged());
	}
	
	public static final String EXPLAIN_UNCAPPED_ROLLS = "The rolls are not limited to the 0-100 range.";
	public static final String EXPLAIN_CAN_DROP_ITEMS = "You can drop carried items with the " + Color.custom("drop", Colors.WHITE) + " command.";
	public static final String EXPLAIN_UPGRADED_SPAWNS = "Items spawn as upgraded.";
	public static final String EXPLAIN_DICE_FUSION = "You can upgrade dice by grabbing another die of the same type.";
	public static final String EXPLAIN_POKER_NIGHT_SPAWNER = "Only poker dealers and trainers are spawned.";
	
	/*
	 * Hacky custom spawner to preserve existing poker hands, replace other items
	 * with poker hands, but not actually spawn any poker hands. Dependant on
	 * convertCarriedPowerupsToMatchSpawnOverride() implementation.
	 */
	public static final Spawner<BasicPowerup> POKER_NIGHT_SPAWNER = new Spawner<BasicPowerup>(
		() -> Stream.of(PokerDealer.info, DicemonTrainer.info)) {

		@Override
		public boolean canSpawn(Predicate<SpawnInfo> predicate) {
			boolean result = super.canSpawn(predicate);
			result |= predicate.test(PokerHand.info);
			return result;
		}
		
		@Override
		public Spawner<BasicPowerup> subSpawner(Predicate<SpawnInfo> predicate, LastSpawn<BasicPowerup> lastSpawn) {
			return new Spawner<BasicPowerup>(() -> Stream.of(PokerHand.info), lastSpawn);
		}
	};

	static {
		POKER_NIGHT_SPAWNER.setDescription(EXPLAIN_POKER_NIGHT_SPAWNER);
	}
	
	public String getExplanation() {
		List<String> list = new ArrayList<String>();
		list.add(winCondition.get().getExplanation());
		if (canDropItems.isChanged()) list.add(EXPLAIN_CAN_DROP_ITEMS);
		if (cappedRolls.isChanged()) list.add(EXPLAIN_UNCAPPED_ROLLS);
		spawnOverride.ifPresent(so -> list.add(so.getDescription()));
		if (upgradedSpawns.isChanged()) list.add(EXPLAIN_UPGRADED_SPAWNS);
		if (diceFusion.isChanged()) list.add(EXPLAIN_DICE_FUSION);
		return StringUtils.join(list, " ");
	}

}
