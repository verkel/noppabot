/*
 * Created on 19.12.2013
 * @author verkel
 */
package noppabot;

import java.util.*;
import java.util.Map.Entry;


public class Rolls {
	private INoppaBot bot;
	private Map<String, Integer> forParticipant = new HashMap<String, Integer>();
	private NavigableSet<Integer> values;
	
	public Rolls(INoppaBot bot) {
		this.bot = bot;
		onRulesChanged();
	}
	
	public void onRulesChanged() {
		// Recreate the values set with different comparator
		// Rules can't change during the roll period, so no need to copy any values
		Comparator<Integer> cmp = bot.getRules().winCondition.rollComparator;
		values = new TreeSet<Integer>(cmp);
	}
	
	public boolean isEmpty() {
		return forParticipant.isEmpty();
	}
	
	public Set<String> participants() {
		return forParticipant.keySet();
	}
	
	public boolean participated(String nick) {
		return forParticipant.containsKey(nick);
	}
	
	public void put(String nick, int roll) {
		forParticipant.put(nick, roll);
		values.add(roll);
	}
	
	public int get(String nick) {
		return forParticipant.get(nick);
	}
	
	public void clear() {
		forParticipant.clear();
		values.clear();
	}
	
	public boolean isWinningRoll(int roll) {
		if (values.isEmpty()) return true;
		else {
			int topRoll = values.first();
			return roll >= topRoll;
		}
	}
	
	public List<String> getWinningRollers() {
		if (values.isEmpty()) return Collections.emptyList();
		else {
			int topRoll = values.first();
			List<String> winningRollers = new ArrayList<String>();
			for (String nick : participants()) {
				int roll = get(nick);
				if (roll == topRoll) winningRollers.add(nick);
			}
			return winningRollers;
		}
	}
	
	public List<Entry<String, Integer>> orderedList() {
		List<Entry<String, Integer>> rollsList = new ArrayList<Entry<String, Integer>>();
		Comparator<Entry<String, Integer>> comp = bot.getRules().winCondition.rollEntryComparator;
		rollsList.addAll(forParticipant.entrySet());
		Collections.sort(rollsList, comp);
		return rollsList;
	}
}
