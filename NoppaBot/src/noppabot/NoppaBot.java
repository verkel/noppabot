package noppabot;
import it.sauronsoftware.cron4j.Scheduler;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.regex.*;

import noppabot.powerups.*;
import noppabot.powerups.Powerups.Powerup;

import org.jibble.pircbot.PircBot;

public class NoppaBot extends PircBot {

	private static final String NICK = "proxyidra^";
	private static final String SERVER = "irc.cs.hut.fi";
	private static final String CHANNEL = "#orp";
	private static final String ROLLRECORDS_PATH = "/data/public_html/public/misc/orp_rolls/rollrecords.json";
	
	private static final String ROLL_PERIOD_START = "0 0 * * *";
	private static final String ROLL_PERIOD_END = "10 0 * * *";
	
//	private static final String ROLL_PERIOD_START = "57 2 * * *";
//	private static final String ROLL_PERIOD_END = "58 2 * * *";
	
	private String[] rollStartMsgs = {
		"Gentlemen, place your rolls!",
		"It's rolltime!",
		"A new day, time for a new roll!",
		"Who will be our lucky winner this time?",
		"Let the rolls commence!",
		"Tonight we select a new certified rolling professional!",
		"The dice are out of the bag!",
		"Experts say that rolling dice makes you 120% more handsome and/or beautiful.",
		"Everyone got their dice ready? Here we go!",
		"Winners do not use drugs. But enchanted dice, they might.",
		"The dice rolling compo starts... now!",
		"d100 is the name and rolling is my game!",
		"1. roll, 2. ???, 3. Profit!",
		"Make rolls, not war. Alternatively, wage war with dice.",
		NICK + " rolls 100! SUPER!! ... No wait, you guys roll now."
	};
	
	private String[] rollEndMsgs = {
		"The winner has been decided! He is %s with the roll %d!",
		"Our tonight's champion has been elected! %s scored the mighty %d points!",
		"The score is settled! This time, %s won with the roll %d.",
		"The dice gods have finalized the outcome! %s won with the roll %d!",
		"The contest is over! Our lucky winner is %s with %d points!",
		"The die has been cast. Tonight, %s is the dice emperor with %d points!",
		"%s won the compo this time, with %d points. Was he lucky, or maybe he was using... the MASTER DIE?!",
		"Using dice is the best way to make decisions. %s knows this best, beating others with his roll %d!",
		"Tonight, %s was the high roller with %d points!",
		"%s beat the others with the outstanding roll of %d!",
		"%s is certified as the new local rolling professional after the superb %d roll!"
	};
	
	private enum State { NORMAL, ROLL_PERIOD, SETTLE_TIE };
	
	public static final Pattern dicePattern = Pattern.compile("(?:.*\\s)?!?d([0-9]+)(?:\\s.*)?");
	public static final Pattern dicePatternWithCustomRoller = Pattern.compile("(?:.*\\s)?!?d([0-9]+) ([\\w]+)");
	
//	private Random random = new Random();
	private Map<String, Random> randoms = new HashMap<String, Random>();
	private Random commonRandom = new Random();
	private Scheduler scheduler = new Scheduler();
	private State state = State.NORMAL;
	private Map<String, Integer> rolls = new HashMap<String, Integer>();
	private Set<String> tiebreakers = new TreeSet<String>();
	
	public Powerup powerup = null;
	public Map<String, Powerup> powerups = new HashMap<String, Powerup>();
	
	public static void main(String[] args) throws Exception {
		new NoppaBot();
	}
	
	
	public NoppaBot() throws Exception {
		super();
		setLogin(NICK);
		setName(NICK);
		connect(SERVER);
		joinChannel(CHANNEL);
//		sendChannel("Eat a happy meal!");
		
		if (isInRollPeriod()) state = State.ROLL_PERIOD;
		
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
		
		schedulePowerupSpawn();
		scheduler.start();
	}

	private void schedulePowerupSpawn() {
		Calendar now = Calendar.getInstance();
		int startHour = now.get(Calendar.HOUR_OF_DAY) + 1;
		
		int minutes = commonRandom.nextInt(60);
		int hours = commonRandom.nextInt(Math.min(5, 24 - startHour)) + startHour;
		int minutes2 = (minutes + 10) % 60;
		int hours2 = minutes2 < minutes ? hours+1 : hours;
		final String pattern = String.format("%d %d * * *", minutes, hours);
		final String expirePattern = String.format("%d %d * * *", minutes2, hours2);
		
		scheduler.schedule(pattern, new Runnable() {
			
			@Override
			public void run() {
				spawnPowerup();
				scheduler.deschedule(pattern);
			}

		});
		
		scheduler.schedule(expirePattern, new Runnable() {
			
			@Override
			public void run() {
				expirePowerup();
				scheduler.deschedule(expirePattern);
				schedulePowerupSpawn();
			}

		});
	}

	private void spawnPowerup() {
		if (powerup != null) return;
		
		powerup = Powerups.getRandom();
		powerup.onSpawn(this);
	}

	private void expirePowerup() {
		if (powerup == null) return;
		
		powerup.onExpire(this);
		powerup = null;
	}
	
	private boolean isInRollPeriod() {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		return (hour == 0 && minute >= 0 && minute < 10);
	}
	
	private boolean isOvertime() {
		return !isInRollPeriod() && state == State.ROLL_PERIOD;
	}
	
	@Override
	protected void onMessage(String channel, String sender, String login, String hostname,
		String message) {
		
		Matcher customRollerMatcher = dicePatternWithCustomRoller.matcher(message);
		Matcher regularMatcher = dicePattern.matcher(message);
		boolean customRoller = customRollerMatcher.matches();
		boolean regularRoll = regularMatcher.matches();
		
		if (customRoller || regularRoll) {
			String numberStr;
			if (customRoller) {
				numberStr = customRollerMatcher.group(1);
				sender = customRollerMatcher.group(2);
			}
			else {
				numberStr = regularMatcher.group(1);
			}

			int sides = Integer.parseInt(numberStr);
			roll(sender, sides);
		}
		else if (message.equalsIgnoreCase("grab") || message.equalsIgnoreCase("pick") 
			|| message.equalsIgnoreCase("take") || message.equalsIgnoreCase("get")) {
			grabPowerup(sender);
		}

	}
	
	private void grabPowerup(String nick) {
		Powerup powerup = this.powerup;
		
		if (powerup == null) {
			sendChannel("Nothing to grab.");
		}
		else {
			powerups.put(nick, powerup);
			powerup.onPickup(this, nick);
			this.powerup = null;
		}
	}
	
	private void roll(String nick, int sides) {
		int value = getRollFor(nick, sides);
		if (nick.equalsIgnoreCase("etsura")) value = -value;
		String msg = null;
		if (sides == 100) {
			
			boolean powerupUsed = false;
			if (powerups.containsKey(nick)) {
				Powerup powerup = powerups.get(nick);
				if (state == State.ROLL_PERIOD) {
					value = powerup.onContestRoll(this, nick, value);
					if (powerup.shouldRemove()) powerups.remove(nick);
					powerupUsed = true;
				}
				else powerup.onNormalRoll(this, nick, value);
			}
			
			if (!powerupUsed) sendDefaultContestRollMessage(nick, value);
			
			participate(nick, value);
		}
		else {
			sendChannelFormat("%s rolls %d!", nick, value);
		}
	}

	public void sendDefaultContestRollMessage(String nick, int value) {
		sendChannel(getDefaultContestRollMessage(nick, value));
	}

	public String getDefaultContestRollMessage(String nick, int value) {
		String participatedMsg = participated(nick) ? 
			" You've already rolled " + participatingRoll(nick) + " though, this roll won't participate!" : "";
		return String.format("%s rolls %d! %s%s", nick, value, grade(value), participatedMsg);
	}
	
	public String grade(int value) {
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
		else if (value == 1) return "Yikes!";
		else return "Huh?";
	}
	
	private void participate(String nick, int rollValue) {
		if (state == State.ROLL_PERIOD && !rolls.containsKey(nick)) {
			rolls.put(nick, rollValue);
			if (isOvertime()) endRollPeriod();
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
			sendChannel("No rolls within 10 minutes. Now first roll wins, be quick!");
//			state = State.NORMAL;
			return;
		}
		else {
			List<String> highestRollers = getHighestRollers(rolls);
			if (highestRollers.size() == 1) {
				String winner = highestRollers.get(0);
				int roll = rolls.get(winner);
				String msg = String.format(randomRollEndMsg(), winner, roll);
				sendChannel(msg);
				
				updateRecords(winner);
				
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
		powerups.clear();
	}
	
	private void updateRecords(String winner) {
		File path = new File(ROLLRECORDS_PATH);
		RollRecords rec;
		if (path.exists()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(path));
				rec = RollRecords.load(reader);
			}
			catch (IOException e) {
				e.printStackTrace();
				return;
			}
			finally {
				close(reader);
			}
		}
		else rec = new RollRecords();
		
		// Ensure record contains every user
		for (String nick : rolls.keySet()) {
			rec.getOrAddUser(nick);
		}
		
		// Increment wins for the winner
		rec.incrementWins(winner);
		
		// Add the today's roll for everyone
		for (User user : rec.users) {
			if (rolls.containsKey(user.nick)) {
				int roll = rolls.get(user.nick);
				user.addRoll(roll);
				user.participation++;
			}
			else {
				user.addRoll(0);
			}
		}
		
		rec.sortUsers();
		
		BufferedWriter writer = null;
		try {
			if (path.exists()) {
				File old = new File(path.toString() + ".old");
				copyFile(path, old);
			}
			
			writer = new BufferedWriter(new FileWriter(path));
			rec.save(writer);
			close(writer);
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
		finally {
			close(writer);
		}
	}

	private String randomRollStartMsg() {
		return rollStartMsgs[commonRandom.nextInt(rollStartMsgs.length)];
	}
	
	private String randomRollEndMsg() {
		return rollEndMsgs[commonRandom.nextInt(rollEndMsgs.length)];
	}

	public void sendChannelFormat(String msg, Object... args) {
		sendChannel(String.format(msg, args));
	}
	
	public void sendChannel(String msg) {
		sendMessage(CHANNEL, msg);
	}
	
	private Random getRandomFor(String nick) {
		if (!randoms.containsKey(nick)) randoms.put(nick, new Random());
		return randoms.get(nick);
	}
	
	public int getRollFor(String nick, int sides) {
		Random random = getRandomFor(nick);
		return random.nextInt(sides)+1;
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
	
	public static void close(Closeable c) {
		try {
			if (c != null) c.close();
		}
		catch (IOException e) {
		}
	}
	
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}
}
