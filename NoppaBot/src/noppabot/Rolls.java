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
		onWinConditionChanged();
	}
	
	public void onWinConditionChanged() {
		// Recreate the values set with different comparator
		Comparator<Integer> cmp = getRollComparator();
		NavigableSet<Integer> newValues = new TreeSet<Integer>(cmp);
		if (values != null) newValues.addAll(values);
		values = newValues;
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
			// The equivalent rolls listed first are currently winning
			return getRollComparator().compare(roll, topRoll) <= 0;
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
		Comparator<Entry<String, Integer>> comp = bot.getRules().winCondition.get().rollEntryComparator;
		rollsList.addAll(forParticipant.entrySet());
		Collections.sort(rollsList, comp);
		return rollsList;
	}
	
	private Comparator<Integer> getRollComparator() {
		return bot.getRules().winCondition.get().rollComparator;
	}
}
