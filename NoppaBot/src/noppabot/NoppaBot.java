package noppabot;
import it.sauronsoftware.cron4j.*;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;

import noppabot.Powerups.Event;
import noppabot.Powerups.FourthWallBreaks;
import noppabot.Powerups.MasterDie;
import noppabot.Powerups.Powerup;

import org.jibble.pircbot.PircBot;

public class NoppaBot extends PircBot implements INoppaBot {

	private final String nick;
	private final String channel;
	private final String server;
	private final File rollRecordsPath;
	private final boolean executeDebugStuff;
	
	private static final String ROLL_PERIOD_START = "0 0 * * *";
	private static final String ROLL_PERIOD_END = "10 0 * * *";
	private static final int POWERUP_EXPIRE_MINUTES = 30;
	
	private Properties properties;
	
	private void loadProperties() throws IOException {
		properties = new Properties();
		BufferedReader r = new BufferedReader(new FileReader("NoppaBot.properties"));
		properties.load(r);
		r.close();
	}
	
	private String[] rollStartMsgs;
	private String[] rollEndMsgs;
	
	private void initMessages() {
		rollStartMsgs = new String[] {
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
			nick + "'s the name and rolling is my game!",
			"1. roll, 2. ???, 3. Profit!",
			"Make rolls, not war. Alternatively, wage war with dice.",
			nick + " rolls 100! SUPER!! ... No wait, you guys roll now.",
			"Kill all audio and non-d100 dice! The rolling compo has begun.",
			"Here we go!",
		};
		
		rollEndMsgs = new String[] {
			"The winner has been decided! It is %s with the roll %d!",
			"Our tonight's champion has been elected! %s scored the mighty %d points!",
			"The score is settled! This time, %s won with the roll %d.",
			"The dice gods have finalized the outcome! %s won with the roll %d!",
			"The contest is over! Our lucky winner is %s with %d points!",
			"The die has been cast. Tonight, %s is the dice emperor with %d points!",
			"%s won the compo this time, with %d points. Was he lucky, or maybe he was using... the MASTER DIE?!",
			"Using dice is the best way to make decisions. %s knows this best, beating others with his roll %d!",
			"Tonight, %s was the high roller with %d points!",
			"%s beat the others with the outstanding roll of %d!",
			"%s is certified as the new local rolling professional after the superb %d roll!",
			"The dice favored %s tonight, granting the victory with %d points!",
			"The demo \"Choose 100 polys and a roll\" by %s entertained all of us tonight. "
			+ "It crushed the competition by a large margin with %d votes!",
			"%s has clicked on lots of candies, cows and cookies, and is experienced in picking the "
			+ "most profitable die, resulting the roll %d!",
		};
	}
	
	private enum State { NORMAL, ROLL_PERIOD, SETTLE_TIE };
	
	public static final Pattern dicePattern = Pattern.compile("(?:.*\\s)?!?d([0-9]+)(?:\\s.*)?");
	public static final Pattern dicePatternWithCustomRoller = Pattern.compile("(?:.*\\s)?!?d([0-9]+) ([^\\s]+)\\s*");
	public static final Pattern commandAndArgument = Pattern.compile("^(\\w+)(?:\\s+(.+)?)?");
	
	private Map<String, PeekableRandom> randoms = new HashMap<String, PeekableRandom>();
	private Random commonRandom = new Random();
	private Scheduler scheduler = new Scheduler();
	private State state = State.NORMAL;
	private Map<String, Integer> rolls = new HashMap<String, Integer>();
	private Set<String> tiebreakers = new TreeSet<String>();
	private List<String> powerupSpawnTaskIDs = new ArrayList<String>();
	//private Powerup powerup = null;
	private List<Powerup> availablePowerups = new ArrayList<Powerup>();
	private Map<String, Powerup> powerups = new TreeMap<String, Powerup>();
	private Set<String> favorsUsed = new HashSet<String>();
	private Set<String> autorolls = new HashSet<String>();
	
	public static void main(String[] args) throws Exception {
		new NoppaBot();
	}
	
	
	public NoppaBot() throws Exception {
		super();
		
		loadProperties();
		nick = properties.getProperty("nick");
		channel = properties.getProperty("channel");
		rollRecordsPath =  new File(properties.getProperty("rollRecordsPath"));
		server = properties.getProperty("server");
		executeDebugStuff = Boolean.parseBoolean(properties.getProperty("executeDebugStuff"));
		
		setLogin(nick);
		setName(nick);
		connect(server);
		joinChannel(channel);
//		sendChannel("Eat a happy meal!");
		
		initMessages();
		
		if (isInRollPeriod()) startRollPeriod();
		
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
		
		schedulePowerupsOfTheDay();
		scheduler.start();
		
		if (executeDebugStuff) debugStuff();
//		giveFreePowerup(); // spawn one right now
		
		handleConsoleCommands();
	}
	
	private void debugStuff() {
//		powerups.put("hassu", new ApprenticeDie());
//		powerups.put("hessu", new ApprenticeDie());
//		powerups.put("kessu", new ApprenticeDie());
//		powerups.put("frodo", new ApprenticeDie());
//		powerups.put("bilbo", new ApprenticeDie());
//		Powerup p = new PolishedDie(); p.initialize(this);
//		powerups.put("Verkel", p);
//		powerup = new DicePirate();
//		powerup.onSpawn(this);
//		rolls.put("jlindval", 100);
		
		availablePowerups.add(new MasterDie());
//		availablePowerups.add(new WeightedDie());
//		availablePowerups.add(new BagOfDice());
//		availablePowerups.add(new Diceteller());
		
		new FourthWallBreaks().run(this);
		
//		startRollPeriod();
	}
	
	private void handleConsoleCommands() {
		System.out.printf("Joined as %s to channel %s\n", nick, channel);
		
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
			String input;
			while ((input = r.readLine()) != null) {
//				String[] tokens = input.split("\\s+");
				
				Matcher commandMatcher = commandAndArgument.matcher(input);
				if (!commandMatcher.matches()) continue;
				String cmd = commandMatcher.group(1);
				String args = commandMatcher.group(2);
				
				if (cmd.equals("quit")) {
					System.out.println("Quitting");
					if (args != null) quit(args);
					else quit("Goodbye!");
					break;
				}
				else if (cmd.equals("help")) {
					commandsHelp();
				}
				else if (cmd.equals("startperiod")) {
					System.out.println("Starting roll period");
					startRollPeriod();
				}
				else if (cmd.equals("endperiod")) {
					System.out.println("Ending roll period");
					endRollPeriod();
				}
				else if (cmd.equals("freebie")) {
					giveFreePowerup();
				}
				else {
					System.out.println("Unknown command: " + cmd);
					commandsHelp();
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void commandsHelp() {
		System.out.println("Commands: help, quit, startperiod, endperiod, freebie");
	}
	
	private void quit(String reason) {
		scheduler.stop();
		quitServer(reason);
		dispose();
	}
	
	private void giveFreePowerup() {
		sendChannel("Spawning item manually");
		scheduleSpawn(null, Powerups.allPowerups, Collections.<Event>emptyList());
	}

	private void schedulePowerupsOfTheDay() {
		Calendar rollPeriodStart = Calendar.getInstance();
		rollPeriodStart.add(Calendar.DATE, 1);
		rollPeriodStart.set(Calendar.HOUR_OF_DAY, 0);
		rollPeriodStart.set(Calendar.MINUTE, 0);
		rollPeriodStart.set(Calendar.SECOND, 0);
		
		Calendar rollPeriodEnd = (Calendar)rollPeriodStart.clone();
		rollPeriodEnd.set(Calendar.MINUTE, 10);
		
		Calendar spawnEndTime = (Calendar)rollPeriodStart.clone();
		spawnEndTime.add(Calendar.MINUTE, -POWERUP_EXPIRE_MINUTES);
		
		Calendar spawnTime = Calendar.getInstance();
		if (spawnTime.get(Calendar.HOUR_OF_DAY) < 10) {
			spawnTime.set(Calendar.HOUR_OF_DAY, 10);
			spawnTime.set(Calendar.MINUTE, 0);
			spawnTime.set(Calendar.SECOND, 0);
		}
		incrementSpawnTime(spawnTime);
		
		List<Event> allowedEvents = Powerups.allEvents;
		while (spawnTime.before(spawnEndTime)) {
			Object spawn = scheduleSpawn(spawnTime, Powerups.allPowerups, allowedEvents);
			// Only allow one 4th wall break per day
			if (spawn instanceof FourthWallBreaks) allowedEvents = Powerups.allEventsMinusFourthWall;
			incrementSpawnTime(spawnTime);
		}
	}

	private void incrementSpawnTime(Calendar spawnTime) {
		int minutesRandomRange = 400 - 15 * spawnTime.get(Calendar.HOUR_OF_DAY);
		int minutesIncr = commonRandom.nextInt(minutesRandomRange);
		spawnTime.add(Calendar.MINUTE, minutesIncr);
	}
	
	private class SpawnTask extends Task {
		public Object spawn;
		public Date time;
		
		public SpawnTask(Date time, List<Powerup> allowedPowerups, List<Event> allowedEvents) {
			this.time = time;
			spawn = Powerups.getRandomPowerupOrEvent(NoppaBot.this, allowedPowerups, allowedEvents);
		}
		
		public Powerup getPowerup() {
			if (spawn instanceof Powerup) return (Powerup)spawn;
			else return null;
		}

		@Override
		public void execute(TaskExecutionContext context) {
			if (spawn instanceof Powerup) {
				Powerup powerup = (Powerup)spawn;
				powerup.onSpawn(NoppaBot.this);
				availablePowerups.add(powerup);
			}
			else {
				Event event = (Event)spawn;
				event.run(NoppaBot.this);
			}
		}
		
		@Override
		public String toString() {
			return String.format("[%tR] %s", time, spawn);
		}
	}
	
	private class ExpireTask extends Task {
		public Powerup powerup;
		
		public ExpireTask(Powerup powerup) {
			this.powerup = powerup;
		}
		
		@Override
		public void execute(TaskExecutionContext context) {
			availablePowerups.remove(powerup);
			powerup.onExpire(NoppaBot.this);
		}
	}
	
	/**
	 * Schedule a powerup/event spawn or spawn it immediately
	 * 
	 * @param spawnTime Spawn time or null for spawn immediately
	 * @param spawnEvents Spawn events too
	 * @param allowFourthWallBreaks Allow the fourth wall falls event
	 */
	@Override
	public Object scheduleSpawn(Calendar spawnTime, List<Powerup> allowedPowerups, List<Event> allowedEvents) {
		boolean immediateSpawn = (spawnTime == null);
		if (immediateSpawn) spawnTime = Calendar.getInstance();
		
		Calendar expireTime = (Calendar)spawnTime.clone();
		expireTime.add(Calendar.MINUTE, POWERUP_EXPIRE_MINUTES);
		
		final String pattern = String.format("%d %d * * *", 
			spawnTime.get(Calendar.MINUTE), spawnTime.get(Calendar.HOUR_OF_DAY));
		final String expirePattern = String.format("%d %d * * *",
			expireTime.get(Calendar.MINUTE), expireTime.get(Calendar.HOUR_OF_DAY));

		SpawnTask spawnTask = new SpawnTask(spawnTime.getTime(), allowedPowerups, allowedEvents);
		Powerup powerup = spawnTask.getPowerup();
		
		if (immediateSpawn) {
			spawnTask.execute(null);
		}
		else {
			String spawnTaskID = scheduler.schedule(pattern, spawnTask);
			powerupSpawnTaskIDs.add(spawnTaskID);
		}
		
		if (powerup != null) {
			ExpireTask expireTask = new ExpireTask(powerup);
			String expireTaskID = scheduler.schedule(expirePattern, expireTask);
			powerupSpawnTaskIDs.add(expireTaskID);
		}
		
//		System.out.printf("Added spawn on %s, expires on %s\n", spawnTime.getTime(), expireTime.getTime());
		
		return spawnTask.spawn;
	}
	
	private void clearPowerupSpawnTasks() {
		for (String id : powerupSpawnTaskIDs) {
			scheduler.deschedule(id);
		}
		powerupSpawnTaskIDs.clear();
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
			String rollerNick;
			if (customRoller) {
				numberStr = customRollerMatcher.group(1);
				rollerNick = customRollerMatcher.group(2);
				
				if (isOnChannel(rollerNick)) {
					sendChannelFormat("%s is on the channel, so you cannot roll for him/her", rollerNick);
					return;
				}
			}
			else {
				numberStr = regularMatcher.group(1);
				rollerNick = sender;
			}

			int sides = Integer.parseInt(numberStr);
			roll(rollerNick, sides);
		}
		
		Matcher commandMatcher = commandAndArgument.matcher(message);
		if (commandMatcher.matches()) {
			String cmd = commandMatcher.group(1);
			String args = commandMatcher.group(2);
			
			if (cmd.equalsIgnoreCase("grab") || cmd.equalsIgnoreCase("pick") 
				|| cmd.equalsIgnoreCase("take") || cmd.equalsIgnoreCase("get")) {
				grabPowerup(sender, args);
			}
			else if (cmd.equalsIgnoreCase("items")) {
				listItems(false);
			}
			else if (cmd.equalsIgnoreCase("rolls")) {
				listRolls();
			}
			else if (cmd.equalsIgnoreCase("peek")) {
				peekNextSpawn(sender);
			}
			else if (cmd.equalsIgnoreCase("autoroll")) {
				autorollFor(sender);
			}
		}
	}
	
	private void autorollFor(String nick) {
		if (!hasFavor(nick)) return;
		favorsUsed.add(nick);
		
		sendChannelFormat("%s: ok, I will roll for you tonight.", nick);
		
		autorolls.add(nick);
	}


	private void peekNextSpawn(String nick) {
		if (!hasFavor(nick)) return;
		favorsUsed.add(nick);
		
		String spawn = null;
		boolean lastSpawn = true;
		for (String id : powerupSpawnTaskIDs) {
			Task task = scheduler.getTask(id);
			if (task instanceof SpawnTask) {
				if (spawn == null) {
					spawn = task.toString();
				}
				else {
					lastSpawn = false;
					break;
				}
			}
		}
		
		StringBuilder buf = new StringBuilder();
		if (spawn != null) {
			buf.append("Next up is: ");
			buf.append(spawn).append(". ");
			if (lastSpawn) {
				buf.append("That will be the last event for today.");
			}
			else {
				buf.append("That won't be the last event, though!");
			}
		}
		else {
			buf.append("No further events will occur today!");
		}
		
		sendChannel(buf.toString());
	}

	private boolean hasFavor(String nick) {
		if (favorsUsed.contains(nick)) {
			sendChannelFormat("%s: you have used your favor for today", nick);
			return false;
		}
		
		return true;
	}
	
	private void listItems(boolean isRollPeriodStart) {
		if (isRollPeriodStart) {
			for (String nick : powerups.keySet()) {
				String powerupName = powerups.get(nick).getName();
				sendChannelFormat("%s has the %s! ", nick, powerupName);
			}
		}
		else {
			if (powerups.isEmpty()) {
				sendChannel("Nobody has any items.");
				return;
			}
			
			StringBuilder buf = new StringBuilder();
			buf.append("Items: ");
			boolean first = true;
			for (String nick : powerups.keySet()) {
				String powerupName = powerups.get(nick).getName();
				if (!first) buf.append(", ");
				buf.append(String.format("%s (%s)", powerupName, nick));
				first = false;
			}
			sendChannel(buf.toString());
		}
	}
	
	private void listRolls() {
		if (state != State.ROLL_PERIOD) {
			sendChannel("Rolls can be listed only during the roll period.");
			return;
		}

		List<Entry<String, Integer>> rollsList = new ArrayList<Entry<String, Integer>>();
		Comparator<Entry<String, Integer>> comp = new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
				return -e1.getValue().compareTo(e2.getValue());
			}
		};
		rollsList.addAll(rolls.entrySet());
		Collections.sort(rollsList, comp);
		
		StringBuilder buf = new StringBuilder();
		buf.append("Rolls: ");
		boolean first = true;
		for (Entry<String, Integer> entry : rollsList) {
			String nick = entry.getKey();
			int roll = entry.getValue();
			if (!first) buf.append(", ");
			buf.append(String.format("%d (%s)", roll, nick));
			first = false;
		}
		if (buf.length() > 0) sendChannel(buf.toString());
		else sendChannel("Nobody has rolled yet.");
	}

	private void grabPowerup(String nick, String powerupName) {
		Powerup powerup = findPowerup(powerupName);

		if (availablePowerups.isEmpty()) {
			sendChannelFormat("%s: nothing to grab.", nick);
		}
		else if (powerup != null && !powerup.canPickUp(this, nick)) {
			// canPickUp(*) will warn the user
		}
		else {
			if (powerup != null) {
				powerup.onPickup(this, nick);
				if (powerup.isCarried()) powerups.put(nick, powerup);
				availablePowerups.remove(powerup);
			}
			// User didn't specify which item to grab
			else if (powerupName == null) {
				String items = join(availablePowerups, ", ");
				sendChannelFormat("%s: There are multiple dice available: %s. Specify which one you want!", nick, items);
			}
			// Unknown item
			else {
				sendChannelFormat("%s: There is no such item available.", nick);
			}
		}
	}
	
	private Powerup findPowerup(String name) {
		if (name == null) {
			if (availablePowerups.size() == 1) return availablePowerups.get(0);
			else return null;
		}
		
		for (Powerup powerup : availablePowerups) {
			if (powerup.getName().toLowerCase().contains(name.toLowerCase())) return powerup;
		}
		
		return null;
	}
	
	private void roll(String nick, int sides) {
		int value = getRollFor(nick, sides);
		if (nick.equalsIgnoreCase("etsura")) value = -value;
		if (sides == 100) {
			
			boolean powerupUsed = false;
			if (powerups.containsKey(nick) && !participated(nick)) {
				Powerup powerup = powerups.get(nick);
				if (state == State.ROLL_PERIOD || state == State.SETTLE_TIE) {
					value = powerup.onContestRoll(this, nick, value);
					powerupUsed = true;
				}
				else {
					value = powerup.onNormalRoll(this, nick, value);
				}
			}
			
			if (!powerupUsed) sendDefaultContestRollMessage(nick, value);
			
			participate(nick, value);
		}
		else {
			sendChannelFormat("%s rolls %d!", nick, value);
		}
	}

	@Override
	public void sendDefaultContestRollMessage(String nick, int value) {
		sendChannel(getDefaultContestRollMessage(nick, value));
	}

	@Override
	public String getDefaultContestRollMessage(String nick, int value) {
		String participatedMsg = participated(nick) ? 
			" You've already rolled " + participatingRoll(nick) + " though, this roll won't participate!" : "";
		return String.format("%s rolls %d! %s%s", nick, value, grade(value), participatedMsg);
	}
	
	@Override
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
	
	@Override
	public void participate(String nick, int rollValue) {
		if (state == State.ROLL_PERIOD && !rolls.containsKey(nick)) {
			rolls.put(nick, rollValue);
			if (isOvertime()) endRollPeriod();
		}
		else if (state == State.SETTLE_TIE && tiebreakers.contains(nick)) {
			if (!rolls.containsKey(nick)) {
				rolls.put(nick, rollValue);
				tiebreakers.remove(nick);
				if (tiebreakers.isEmpty()) endRollPeriod();
				else {
					String pendingTiebreakers = join(tiebreakers, ", ");
					sendChannelFormat("I still need a roll from %s.", pendingTiebreakers);
				}
			}
		}
	}
	
	@Override
	public boolean participated(String nick) {
		return rolls.containsKey(nick);
	}
	
	private int participatingRoll(String nick) {
		return rolls.get(nick);
	}
	
	private void startRollPeriod() {
		state = State.ROLL_PERIOD;
		sendChannel(randomRollStartMsg());
		listItems(true);
		
		for (String nick : powerups.keySet()) {
			Powerup powerup = powerups.get(nick);
			powerup.onRollPeriodStart(this, nick);
		}
		
		if (!autorolls.isEmpty()) {
			sendChannelFormat("I'll roll for: %s", join(autorolls, ", "));
			
			for (String nick : autorolls) {
				roll(nick, 100);
			}
		}
	}
	
	private void endRollPeriod() {
		
		if (rolls.isEmpty()) {
			sendChannel("No rolls within 10 minutes. Now first roll wins, be quick!");
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
				
				rolls.clear();
				tiebreakers.clear();
				powerups.clear();
				favorsUsed.clear();
				autorolls.clear();
				clearPowerupSpawnTasks();
				schedulePowerupsOfTheDay();
				
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
				
				rolls.clear();
				
				for (String tiebreakerNick : tiebreakers) {
					Powerup powerup = powerups.get(tiebreakerNick);
					powerup.onTiebreakPeriodStart(this, tiebreakerNick);
				}
				
				state = State.SETTLE_TIE;
			}
		}
	}
	
	private void updateRecords(String winner) {
		RollRecords rec = loadRollRecords();
		if (rec == null) return;
		
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
			if (rollRecordsPath.exists()) {
				File old = new File(rollRecordsPath.toString() + ".old");
				copyFile(rollRecordsPath, old);
			}
			
			writer = new BufferedWriter(new FileWriter(rollRecordsPath));
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


	@Override
	public RollRecords loadRollRecords() {
		RollRecords rec;
		if (rollRecordsPath.exists()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(rollRecordsPath));
				rec = RollRecords.load(reader);
			}
			catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			finally {
				close(reader);
			}
		}
		else rec = new RollRecords();
		return rec;
	}

	private String randomRollStartMsg() {
		return rollStartMsgs[commonRandom.nextInt(rollStartMsgs.length)];
	}
	
	private String randomRollEndMsg() {
		return rollEndMsgs[commonRandom.nextInt(rollEndMsgs.length)];
	}

	@Override
	public void sendChannelFormat(String msg, Object... args) {
		sendChannel(String.format(msg, args));
	}
	
	@Override
	public void sendChannel(String msg) {
		sendMessage(channel, msg);
	}
	
	@Override
	public void sendMessageFormat(String nick, String msg, Object... args) {
		sendMessage(nick, String.format(msg, args));
	}
	
	@Override
	public Map<String, Powerup> getPowerups() {
		return powerups;
	}
	
	@Override
	public Map<String, Integer> getRolls() {
		return rolls;
	}
	
	private PeekableRandom getRandomFor(String nick) {
		if (!randoms.containsKey(nick)) randoms.put(nick, new PeekableRandom());
		return randoms.get(nick);
	}
	
	@Override
	public int getRollFor(String nick, int sides) {
		PeekableRandom random = getRandomFor(nick);
		return random.nextInt(sides) + 1;
	}
	
	@Override
	public int peekRollFor(String nick) {
		PeekableRandom random = getRandomFor(nick);
		return random.peek() + 1;
	}
	
	public int countPowerups(Class<? extends Powerup> type) {
		int count = 0;
		for (Powerup powerup : powerups.values()) {
			if (type.isAssignableFrom(powerup.getClass())) count++;
		}
		return count;
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
	
	@Override
	public int getSecondsAfterMidnight() {
		Calendar c = Calendar.getInstance();
		long now = c.getTimeInMillis();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long passed = now - c.getTimeInMillis();
		long secondsPassed = passed / 1000;
		return (int)secondsPassed;
	}
	
	@Override
	public String remainingSpawnsInfo() {
		StringBuilder buf = new StringBuilder();
		boolean first = true;
		for (String id : powerupSpawnTaskIDs) {
			Task task = scheduler.getTask(id);
			if (task instanceof SpawnTask) {
				if (!first) buf.append(", ");
				buf.append(task);
				first = false;
			}
		}
		
		return buf.toString();
	}
	
	private boolean isOnChannel(String nick) {
		org.jibble.pircbot.User[] users = getUsers(channel);
		for (org.jibble.pircbot.User user : users) {
			if (user.getNick().equals(nick)) return true;
		}
		return false;
	}
	
	public static String join(Iterable<?> s, String delimiter) {
	    Iterator<?> iter = s.iterator();
	    if (!iter.hasNext()) return "";
	    StringBuilder buffer = new StringBuilder(iter.next().toString());
	    while (iter.hasNext()) buffer.append(delimiter).append(iter.next().toString());
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
