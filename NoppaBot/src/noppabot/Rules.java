/*
 * Created on 17.12.2013
 * @author verkel
 */
package noppabot;

import java.util.*;
import java.util.Map.Entry;

import noppabot.spawns.*;

import org.jibble.pircbot.Colors;


public class Rules {
	public abstract class WinCondition { // implements Comparator<Entry<String, Integer>> {
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
	
	public final WinCondition ROLL_CLOSEST_TO_TARGET = new WinCondition() {
		@Override
		public void onRollPeriodStart(INoppaBot bot) {
			bot.sendChannelFormat("Tonight we are looking for the roll which is closest " +
				"to number %d!", rollTarget);
		}
		
		@Override
		public int assignScore(int roll) {
			return 100 - Math.abs(rollTarget.getValue() - roll);
		}
		
		@Override
		public void onContestRoll(INoppaBot bot, String nick, int roll) {
			int dist = Math.abs(rollTarget.getValue() - roll);
			if (dist > 0) bot.sendChannelFormat("%s's roll is %d points off the target", nick, dist);
			else bot.sendChannelFormat("%s's roll hit the target!", nick);
		}
		
		@Override
		public String getExplanation() {
			return String.format("The roll closest to number %d wins the contest!", rollTarget);
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
	 * Roll closest to this number wins
	 */
	public final OptionalProperty<Integer> rollTarget = OptionalProperty.create();
	
	/**
	 * Items can be dropped to ground at any time
	 */
	public final Property<Boolean> canDropItems = Property.of(false);

	/**
	 * Item spawner to be used instead of the regular spawner
	 */
	public OptionalProperty<Spawner<BasicPowerup>> spawnOverride = OptionalProperty.create();

	private List<Property<?>> all = Arrays.asList(cappedRolls, winCondition, rollTarget,
		canDropItems, spawnOverride);

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
		if (changed) bot.onRulesChanged();
		return changed;
	}

	private void doReset() {
		all.forEach(rule -> rule.reset());
	}
	
	private boolean isChanged() {
		return all.stream().anyMatch(rule -> rule.isChanged());
	}
	
	public static final String EXPLAIN_UNCAPPED_ROLLS = "The rolls are not limited to the 0-100 range.";
	public static final String EXPLAIN_CAN_DROP_ITEMS = "You can drop carried items with the " + Color.custom("drop", Colors.WHITE) + " command.";
	
	public String getExplanation() {
		List<String> list = new ArrayList<String>();
		list.add(winCondition.get().getExplanation());
		if (canDropItems.isChanged()) list.add(EXPLAIN_CAN_DROP_ITEMS);
		if (cappedRolls.isChanged()) list.add(EXPLAIN_UNCAPPED_ROLLS);
		spawnOverride.ifPresent(so -> list.add(so.getDescription()));
		return StringUtils.join(list, " ");
	}
}
