package noppabot;
import it.sauronsoftware.cron4j.Scheduler;

import java.util.*;
import java.util.regex.*;

import org.jibble.pircbot.PircBot;

public class NoppaBot extends PircBot {

	private static final String NICK = "proxyidra^";
	private static final String SERVER = "irc.cs.hut.fi";
	private static final String CHANNEL = "#orp";
	
	private static final String ROLL_PERIOD_START = "0 0 * * *";
	private static final String ROLL_PERIOD_END = "10 0 * * *";
	
//	private static final String ROLL_PERIOD_START = "57 2 * * *";
//	private static final String ROLL_PERIOD_END = "58 2 * * *";
	
	private String[] rollStartMsgs = {
		"Gentlemen, place your rolls!",
		"It's rolltime!",
		"A new day, time for a new roll!",
		"Who will be our lucky winner this time?",
		"Let the rolls commence!"
	};
	
	private String[] rollEndMsgs = {
		"The winner has been decided! He is %s with the roll %d!",
		"Our tonight's champion has been elected! %s scored the mighty %d points!",
		"The score is settled! This time, %s won with the roll %d.",
		"The dice gods have finalized the outcome! %s won with the roll %d!",
		"The contest is over! Our lucky winner is %s with %d points!"
	};
	
	private enum State { NORMAL, ROLL_PERIOD, SETTLE_TIE };
	
	public static final Pattern dicePattern = Pattern.compile("(?:.*\\s)?!?d([0-9]+)(?:\\s.*)?");
	public static final Pattern dicePatternWithCustomRoller = Pattern.compile("(?:.*\\s)?!?d([0-9]+) ([\\w]+)");
	
	private Random random = new Random();
	private Scheduler scheduler = new Scheduler();
	private State state = State.NORMAL;
	private Map<String, Integer> rolls = new HashMap<String, Integer>();
	private Set<String> tiebreakers = new TreeSet<String>();
	
	public static void main(String[] args) throws Exception {
		new NoppaBot();
	}
	
	
	public NoppaBot() throws Exception {
		super();
		setLogin(NICK);
		setName(NICK);
		connect(SERVER);
		joinChannel(CHANNEL);
		sendChannel("Hi everybody!!");
		
		scheduler.schedule(ROLL_PERIOD_START, new Runnable() {
			@Override
			public void run() {
				startRollPeriod();
			}
		});
		
		scheduler.schedule(ROLL_PERIOD_END, new Runnable() {
			@Override
			public void run() {
				endRollPeriod();
			}
		});
		
		scheduler.start();
	}
	
	@Override
	protected void onMessage(String channel, String sender, String login, String hostname,
		String message) {
		
		String numberStr;
		Matcher customRollerMatcher = dicePatternWithCustomRoller.matcher(message);
		Matcher regularMatcher = dicePattern.matcher(message);
		
		if (customRollerMatcher.matches()) {
			numberStr = customRollerMatcher.group(1);
			sender = customRollerMatcher.group(2);
		}
		else if (regularMatcher.matches()) {
			numberStr = regularMatcher.group(1);
		}
		else return;
		
		int sides = Integer.parseInt(numberStr);
		roll(sender, sides);
	}
	
	private void roll(String nick, int sides) {
		int value = random.nextInt(sides)+1;
		String msg;
		if (sides == 100) {
			String participatedMsg = participated(nick) ? 
				" You've already rolled " + participatingRoll(nick) + " though, this roll won't participate!" : "";
			msg = String.format("%s rolls %d! %s%s", nick, value, grade(value), participatedMsg);
			participate(nick, value);
		}
		else {
			msg = String.format("%s rolls %d!", nick, value);
		}
		sendChannel(msg);
	}
	
	private String grade(int value) {
		if (value == 100) return "You showed us....the ULTIMATE roll!";
		else if (value >= 95) return "You are a super roller!";
		else if (value >= 90) return "Amazing!";
		else if (value >= 80) return "Nicely done!";
		else if (value >= 70) return "Quite good!";
		else if (value >= 60) return "That's ok!";
		else if (value >= 50) return "... That's decent!";
		else if (value >= 40) return "... Could've gone better!";
		else if (value >= 30) return "That's kind of a low roll!";
		else if (value >= 20) return "Not having much luck today? :(";
		else if (value >= 10) return "Still at two digits!";
		else if (value > 1) return "Seek some advice from your local qualified rolling professional!";
		else /*if (sides == 1)*/ return "Yikes!";
	}
	
	private void participate(String nick, int rollValue) {
		if (state == State.ROLL_PERIOD && !rolls.containsKey(nick)) {
			rolls.put(nick, rollValue);
		}
		else if (state == State.SETTLE_TIE && tiebreakers.contains(nick)) {
			if (!rolls.containsKey(nick)) {
				rolls.put(nick, rollValue);
				tiebreakers.remove(nick);
				if (tiebreakers.isEmpty()) endRollPeriod();
			}
		}
	}
	
	private boolean participated(String nick) {
		return rolls.containsKey(nick);
	}
	
	private int participatingRoll(String nick) {
		return rolls.get(nick);
	}
	
	private void startRollPeriod() {
		state = State.ROLL_PERIOD;
		sendChannel(randomRollStartMsg());
	}
	
	private void endRollPeriod() {
		
		if (rolls.isEmpty()) {
			sendChannel("No rolls today :(");
			state = State.NORMAL;
		}
		else {
			List<String> highestRollers = getHighestRollers(rolls);
			if (highestRollers.size() == 1) {
				String winner = highestRollers.get(0);
				int roll = rolls.get(winner);
				String msg = String.format(randomRollEndMsg(), winner, roll);
				sendChannel(msg);
				state = State.NORMAL;
			}
			else {
				String tiebreakersStr = join(highestRollers, ", ");
				int roll = rolls.get(highestRollers.get(0));
				String msg = String.format(
					"The roll %d was tied between %s. Roll again to settle the score!", 
					roll, tiebreakersStr);
				sendChannel(msg);
				tiebreakers.addAll(highestRollers);
				state = State.SETTLE_TIE;
			}
		}
		
		rolls.clear();
		tiebreakers.clear();
	}
	
	private String randomRollStartMsg() {
		return rollStartMsgs[random.nextInt(rollStartMsgs.length)];
	}
	
	private String randomRollEndMsg() {
		return rollEndMsgs[random.nextInt(rollEndMsgs.length)];
	}
	
	private void sendChannel(String msg) {
		sendMessage(CHANNEL, msg);
	}
	
	public static List<String> getHighestRollers(Map<String, Integer> rolls) {
		int max = Integer.MIN_VALUE;
		List<String> highestRollers = new ArrayList<String>();
		for (String nick : rolls.keySet()) {
			int roll = rolls.get(nick);
			if (roll > max) {
				max = roll;
				highestRollers.clear();
				highestRollers.add(nick);
			}
			else if (roll == max) {
				highestRollers.add(nick);
			}
		}
		
		return highestRollers;
	}
	
	public static String join(Iterable<? extends CharSequence> s, String delimiter) {
	    Iterator<? extends CharSequence> iter = s.iterator();
	    if (!iter.hasNext()) return "";
	    StringBuilder buffer = new StringBuilder(iter.next());
	    while (iter.hasNext()) buffer.append(delimiter).append(iter.next());
	    return buffer.toString();
	}
}
