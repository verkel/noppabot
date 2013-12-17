/*
 * Created on 17.12.2013
 * @author verkel
 */
package noppabot;

import java.util.*;
import java.util.Map.Entry;


public class Rules {
	public abstract class WinCondition implements Comparator<Entry<String, Integer>> {
		public abstract int assignScore(int roll);
		
		@Override
		public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
			return compare(e1.getValue(), e2.getValue());
		}
		
		protected int compare(int roll1, int roll2) {
			return -MathUtils.compare(assignScore(roll1), assignScore(roll2));
		}
		
		/**
		 * Here we should explain non-standard rules
		 */
		public void onRollPeriodStart(INoppaBot bot) {
		}
		
		/**
		 * Here we should tell how the roll is graded
		 */
		public void onContestRoll(INoppaBot bot, String nick, int roll) {
		}
	}
	
	public final WinCondition HIGHEST_ROLL = new WinCondition() {
		
		@Override
		public int assignScore(int roll) {
			return roll;
		}
	};
	
	public final WinCondition LEAST_ROLL = new WinCondition() {
		@Override
		public void onRollPeriodStart(INoppaBot bot) {
			bot.sendChannel("Tonight we are looking for the least roll!");
		}
		
		@Override
		public int assignScore(int roll) {
			return 100 - roll;
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
			return 100 - Math.abs(rollTarget - roll);
		}
		
		@Override
		public void onContestRoll(INoppaBot bot, String nick, int roll) {
			int dist = Math.abs(rollTarget - roll);
			if (dist > 0) bot.sendChannelFormat("%s's roll is %d points off the target", nick, dist);
			else bot.sendChannelFormat("%s's roll hit the target!");
		}
	};
	
	/**
	 * Rolls have a 0..100 point cap
	 */
	public boolean cappedRolls;
	
	/**
	 * How is the winner determined
	 */
	public WinCondition winCondition;
	
	/**
	 * Roll closest to this number wins
	 */
	public int rollTarget;
	
	public final boolean cappedRollsDefault = true;
	public final WinCondition winConditionDefault = HIGHEST_ROLL;
	public final int rollTargetDefault = -1;
	
	public Rules() {
		reset();
	}
	
	public void onRollPeriodStart(INoppaBot bot) {
		if (cappedRolls != cappedRollsDefault) {
			bot.sendChannel("Remember, the rolls are not restricted to the 0-100 range now.");
		}
		if (winCondition != winConditionDefault) {
			winCondition.onRollPeriodStart(bot);
		}
	}
	
	/**
	 * Reset the rules
	 * 
	 * @return if the rules changed
	 */
	public boolean reset() {
		boolean changed =  (cappedRolls != cappedRollsDefault || winCondition != winConditionDefault 
			|| rollTarget != rollTargetDefault);
		
		cappedRolls = cappedRollsDefault;
		winCondition = winConditionDefault;
		rollTarget = rollTargetDefault;
		
		return changed;
	}
}
