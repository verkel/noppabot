/*
 * Created on 19.12.2013
 * @author verkel
 */
package noppabot;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;


public class Rolls {
	private INoppaBot bot;
	private Map<String, Roll> forParticipant = new HashMap<String, Roll>();
	private NavigableSet<Roll> values;
	
	public Rolls(INoppaBot bot) {
		this.bot = bot;
		onWinConditionChanged();
	}
	
	public void onWinConditionChanged() {
		// Recreate the values set with different comparator
		Comparator<Roll> cmp = getRollComparator();
		NavigableSet<Roll> newValues = new TreeSet<Roll>(cmp);
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
	
	public void put(String nick, Roll roll) {
		forParticipant.put(nick, roll);
		values.add(roll);
	}
	
	public Roll get(String nick) {
		return forParticipant.get(nick);
	}
	
	public void clear() {
		forParticipant.clear();
		values.clear();
	}
	
	public boolean isWinningRoll(Roll roll) {
		if (values.isEmpty()) return true;
		else {
			Roll topRoll = values.first();
			// The equivalent rolls listed first are currently winning
			return getRollComparator().compare(roll, topRoll) <= 0;
		}
	}
	
	public List<String> getWinningRollers() {
		if (values.isEmpty()) return Collections.emptyList();
		else {
			Roll topRoll = values.first();
			List<String> winningRollers = new ArrayList<String>();
			for (String nick : participants()) {
				Roll roll = get(nick);
				if (roll == topRoll) winningRollers.add(nick);
			}
			return winningRollers;
		}
	}
	
	public List<Entry<String, Roll>> orderedList() {
//		List<Entry<String, Roll>> rollsList = new ArrayList<Entry<String, Roll>>();
		Comparator<Entry<String, Roll>> comp = bot.getRules().winCondition.get().rollEntryComparator;
//		rollsList.addAll(forParticipant.entrySet());
		
//		Collections.sort(rollsList, comp);
//		return rollsList;
		
		return forParticipant.entrySet().stream().sorted(comp).collect(Collectors.toList());
	}
	
	private Comparator<Roll> getRollComparator() {
		return bot.getRules().winCondition.get().rollComparator;
	}
}
