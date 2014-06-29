package noppabot;
import it.sauronsoftware.cron4j.*;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;

import noppabot.StringUtils.StringConverter;
import noppabot.spawns.*;
import noppabot.spawns.dice.*;
import noppabot.spawns.dice.PokerHand.BetterHand;
import noppabot.spawns.events.FourthWallBreaks;
import noppabot.spawns.instants.*;
import noppabot.spawns.instants.TrollingProfessional.Bomb;

import org.jibble.pircbot.*;

import ca.ualberta.cs.poker.Deck;

import com.google.common.collect.Iterables;

public class NoppaBot extends PircBot implements INoppaBot {

	private final String botNick;
	private final String channel;
	private final String server;
	private final File rollRecordsPath;
	private final boolean debug;
	private final boolean executeDebugStuff;
	private final boolean dryRun;
	
	private static final String ROLL_PERIOD_ABOUT_TO_START = "59 23 * * *";
	private static final String ROLL_PERIOD_START = "0 0 * * *";
	private static final String AUTOROLL_TIME = "5 0 * * *";
	private static final String ROLL_PERIOD_END = "10 0 * * *";
	private static final int POWERUP_EXPIRE_MINUTES = 60;
	
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
			botNick + "'s the name and rolling is my game!",
			"1. roll, 2. ???, 3. Profit!",
			"Make rolls, not war. Alternatively, wage war with dice.",
			botNick + " rolls 100! SUPER!! ... No wait, you guys roll now.",
			"Kill all audio and non-d100 dice! The rolling compo has begun.",
			"Here we go!",
		};
		
		rollEndMsgs = new String[] {
			"The winner has been decided! It is %s with the roll %s!",
			"Our tonight's champion has been elected! %s scored the mighty %s points!",
			"The score is settled! This time, %s won with the roll %s.",
			"The dice gods have finalized the outcome! %s won with the roll %s!",
			"The contest is over! Our lucky winner is %s with %s points!",
			"The die has been cast. Tonight, %s is the dice emperor with %s points!",
			"%s won the compo this time, with %s points. Was he lucky, or maybe he was using... the MASTER DIE?!",
			"Using dice is the best way to make decisions. %s knows this best, beating others with his roll %s!",
			"Tonight, %s was the high roller with %s points!",
			"%s beat the others with the outstanding roll of %s!",
			"%s is certified as the new local rolling professional after the superb %s roll!",
			"The dice favored %s tonight, granting the victory with %s points!",
			"The demo \"Choose 100 polys and a roll\" by %s entertained all of us tonight. "
			+ "It crushed the competition by a large margin with %s votes!",
			"%s has clicked on lots of candies, cows and cookies, and is experienced in picking the "
			+ "most profitable die, resulting the roll %s!",
		};
	}
	
	public static final Pattern dicePattern = Pattern.compile("^d([0-9]+)");
	public static final Pattern dicePatternWithCustomRoller = Pattern.compile("^d([0-9]+) ([^\\s]+)");
	public static final Pattern commandAndArgument = Pattern.compile("^(\\w+)(?:\\s+(.+)?)?");
	
	// Pircbot onMessage callback and scheduled tasks should synchronize on this lock
	private Object lock = new Object();
	
	private Rules rules;
	private Map<String, PeekableRandom> randoms = new HashMap<String, PeekableRandom>();
	private Random commonRandom = new Random();
	private Scheduler scheduler = new Scheduler();
	private State state = State.NORMAL;
	private Rolls rolls;
	private Set<String> tiebreakers = new TreeSet<String>();
	private NavigableSet<SpawnTask> spawnTasks = new TreeSet<SpawnTask>();
	private Set<ExpireTask> expireTasks = new HashSet<ExpireTask>();
	private SettleTieTimeoutTask settleTieTimeoutTask;
	private SortedSet<Powerup> availablePowerups = new TreeSet<Powerup>(new SpawnableComparator());
	private Map<String, Powerup> powerups = new TreeMap<String, Powerup>();
	private Set<String> favorsUsed = new HashSet<String>();
	private Set<String> autorolls = new HashSet<String>();
	private Calendar lastTiebreakPeriodStartTime;
	private Calendar rollPeriodStartTime;
	private Calendar rollPeriodEndTime;
	private Calendar spawnEndTime;
	private PokerTable pokerTable = new PokerTable(this);
	
	public static void main(String[] args) throws Exception {
		new NoppaBot();
	}
	
	
	public NoppaBot() throws Exception {
		super();
		
		loadProperties();
		botNick = properties.getProperty("nick");
		channel = properties.getProperty("channel");
		rollRecordsPath =  new File(properties.getProperty("rollRecordsPath"));
		server = properties.getProperty("server");
		debug = Boolean.parseBoolean(properties.getProperty("debug"));
		executeDebugStuff = Boolean.parseBoolean(properties.getProperty("executeDebugStuff"));
		dryRun = Boolean.parseBoolean(properties.getProperty("dryRun"));
		
		setVerbose(false); // Print stacktraces, but also useless server messages
		setLogin(botNick);
		setName(botNick);
		
		rules = new Rules(this);
		rolls = new Rolls(this);
		
		initMessages();
		
		if (isInRollPeriod()) startRollPeriod();
		
		scheduler.schedule(ROLL_PERIOD_ABOUT_TO_START, new Runnable() {
			@Override
			public void run() {
				synchronized (lock) {
					doTasksBeforeRollPeriodStart();
				}
			}
		});
		
		scheduler.schedule(ROLL_PERIOD_START, new Runnable() {
			@Override
			public void run() {
				synchronized (lock) {
					startRollPeriod();
				}
			}
		});
		
		scheduler.schedule(AUTOROLL_TIME, new Runnable() {
			@Override
			public void run() {
				synchronized (lock) {
					autorollFor(autorolls);
				}
			}
		});
		
		scheduler.schedule(ROLL_PERIOD_END, new Runnable() {
			@Override
			public void run() {
				synchronized (lock) {
					tryEndRollPeriod();
				}
			}
		});
		
		schedulePowerupsOfTheDay();
		scheduler.start();
		
		if (executeDebugStuff) debugStuff();
//		giveFreePowerup(); // spawn one right now
		
		if (dryRun) {
			scheduler.stop();
			System.out.println("Dry run complete. Quitting.");
		}
		else {
			connect(server);
			joinChannel(channel);
			handleConsoleCommands();
		}
	}


	@SuppressWarnings("unused")
	private void debugListCards() {
		int i = 0;
		StringBuilder[] sb = new StringBuilder[4];
		for (int j = 0; j < sb.length; j++) sb[j] = new StringBuilder();
		Deck deck = new Deck();
		while (deck.cardsLeft() > 0) {
			sb[i / 13].append(deck.deal()).append(" ");
			i++;
		}
		for (int j = 0; j < sb.length; j++) sendChannelFormat("Cards %d: %s", j, sb[j]);
	}
	
	private void debugStuff() {
//		for (int i = 0; i < 5; i++) new RulesChange().run(this);
		
//		for (int i = 0; i < 10; i++) availablePowerups.add(new DicemonTrainer().initialize(this));
		
//		BasicPowerup cards = new PokerCards().initialize(this);
//		cards.setOwner("Verkel");
//		powerups.put("Verkel", cards);
//		powerups.put("kessu", new ApprenticeDie().initialize(this));
//		powerups.put("frodo", new FastDie().initialize(this));
//		powerups.put("bilbo", new MasterDie().initialize(this));
//		Powerup p = new VariableDie(); p.initialize(this);
//		powerups.put("Verkel", p);
//		powerup = new DicePirate();
//		powerup.onSpawn(this);
//		rolls.put("Verkel", 100);
//		rolls.put("hassu", 78);
//		rolls.put("frodo", 78);
//		rolls.put("bilbo", 61);
//		rolls.put("noob", 3);
//		autorolls.add("hassu");
//		autorolls.add("frodo");
//		autorolls.add("bilbo");
		
//		new RulesChange().run(this);
//		rules.cappedRolls = false;
//		onRulesChanged();
		
//		availablePowerups.add(p);
//		availablePowerups.add(new TrollingProfessional());
//		availablePowerups.add(new BagOfDice());
//		availablePowerups.add(new DicemonTrainer());
//		availablePowerups.add(new WeightedDie());
//		availablePowerups.add(new BagOfDice());
//		availablePowerups.add(new Diceteller());
		availablePowerups.add(new HumongousDie().initialize(this));
		availablePowerups.add(new HumongousDie().initialize(this));
		
		availablePowerups.add(new ApprenticeDie().initialize(this));
		
//		new FourthWallBreaks().run(this);
		
		spawnAllPowerups();
		
		rules.canDropItems = true;
		onRulesChanged();
		
		setNextRoll("Verkel", 100, 13);
		
//		scheduleSpawn(null, new PokerDealer().initialize(this));
		
//		grabPowerup("Verkel", PokerDealer.NAME);
	}
	
	private void spawnAllPowerups() {
		for (BasicPowerup powerup : Powerups.allPowerups) {
			powerup.initialize(this);
			availablePowerups.add(powerup);
		}
	}
	
	private void handleConsoleCommands() {
		System.out.printf("Joined as %s to channel %s\n", botNick, channel);
		
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
			String input;
			boolean quit = false;
			while (!quit && (input = r.readLine()) != null) {
				Matcher commandMatcher = commandAndArgument.matcher(input);
				if (!commandMatcher.matches()) continue;
				String cmd = commandMatcher.group(1);
				String args = commandMatcher.group(2);
				
				synchronized (lock) {
					quit = handleConsoleCommand(cmd, args);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean handleConsoleCommand(String cmd, String args) {
		if (cmd.equals("quit")) {
			System.out.println("Quitting");
			if (args != null) quit(args);
			else quit("Goodbye!");
			return true;
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
			tryEndRollPeriod();
		}
		else if (cmd.equals("freebie")) {
			giveFreePowerup();
		}
		else {
			System.out.println("Unknown command: " + cmd);
			commandsHelp();
		}
		
		return false;
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
		scheduleRandomSpawn(null, Powerups.allPowerups, null);
	}

	private void schedulePowerupsOfTheDay() {
		computeTimes();
		Calendar spawnTime = Calendar.getInstance();
		if (debug) {
			spawnTime.set(Calendar.HOUR_OF_DAY, 0);
			spawnTime.set(Calendar.MINUTE, 0);
			spawnTime.set(Calendar.SECOND, 0);
		}
//		else {
//			System.out.println("Scheduling spawns at non-midnight, " + spawnTime.getTime());
//		}
		incrementSpawnTime(spawnTime);
		
		Spawner<BasicPowerup> spawnPowerups = Powerups.firstPowerup;
		Spawner<Event> spawnEvents = Powerups.allEvents;
		int n = 0;
		while (spawnTime.before(spawnEndTime)) {
			if (n > 0) spawnPowerups = Powerups.allPowerups;
			if (spawnTime.get(Calendar.HOUR_OF_DAY) >= 16) spawnEvents = Powerups.lateEvents;
			ISpawnable spawn = scheduleRandomSpawn(spawnTime, spawnPowerups, spawnEvents).spawn;
			// Only allow one 4th wall break per day
			if (spawn instanceof FourthWallBreaks) spawnEvents = Powerups.allEventsMinusFourthWall;
			incrementSpawnTime(spawnTime);
			n++;
		}
	}


	private void computeTimes() {
		rollPeriodStartTime = Calendar.getInstance();
		rollPeriodStartTime.add(Calendar.DATE, 1);
		rollPeriodStartTime.set(Calendar.HOUR_OF_DAY, 0);
		rollPeriodStartTime.set(Calendar.MINUTE, 0);
		rollPeriodStartTime.set(Calendar.SECOND, 0);
		
		rollPeriodEndTime = (Calendar)rollPeriodStartTime.clone();
		rollPeriodEndTime.set(Calendar.MINUTE, 10);
		
		spawnEndTime = (Calendar)rollPeriodStartTime.clone();
		spawnEndTime.add(Calendar.MINUTE, -1);
	}

	private void incrementSpawnTime(Calendar spawnTime) {
		int minutesRandomRange = 260 - 5 * spawnTime.get(Calendar.HOUR_OF_DAY);
		int minutesIncr = commonRandom.nextInt(minutesRandomRange);
		spawnTime.add(Calendar.MINUTE, minutesIncr);
	}
	
	public class SpawnTask extends Task implements Comparable<SpawnTask>, IColorStrConvertable {
		public ISpawnable spawn;
		public Date time;
		public String id;
		public ExpireTask expireTask;
		
		public SpawnTask(Date time, ISpawnable spawn) {
			this.time = time;
			this.spawn = spawn;
		}
		
		public BasicPowerup getPowerup() {
			if (spawn instanceof BasicPowerup) return (BasicPowerup)spawn;
			else return null;
		}

		@Override
		public void execute(TaskExecutionContext context) {
			synchronized (lock) {
				spawnTasks.remove(this);
				scheduler.deschedule(id);
				
				if (spawn instanceof BasicPowerup) {
					BasicPowerup powerup = (BasicPowerup)spawn;
					powerup.onSpawn();
					availablePowerups.add(powerup);
				}
				else {
					Event event = (Event)spawn;
					event.run(NoppaBot.this);
				}
			}
		}
		
		@Override
		public String toString() {
			return String.format("[%tR] %s", time, spawn);
		}
		
		@Override
		public String toStringColored() {
			return String.format("[%tR] %s", time, spawn.toStringColored());
		}

		@Override
		public int compareTo(SpawnTask other) {
			return this.time.compareTo(other.time);
		}
	}
	
	public class ExpireTask extends Task {
		public Powerup powerup;
		public String id;
		
		public ExpireTask(Powerup powerup) {
			this.powerup = powerup;
		}
		
		@Override
		public void execute(TaskExecutionContext context) {
			synchronized (lock) {
				expireTasks.remove(this);
				scheduler.deschedule(id);
				
				expirePowerup(powerup);
			}
		}
	}
	
	private void expirePowerup(Powerup powerup) {
		if (availablePowerups.remove(powerup)) {
			powerup.onExpire();
		}
	}

	private void expireAllPowerups() {
		for (Powerup powerup : availablePowerups) {
			powerup.onExpire();
		}
		availablePowerups.clear();
	}

	@Override
	public SpawnTask scheduleRandomSpawn(Calendar spawnTime, Spawner<BasicPowerup> allowedPowerups, Spawner<Event> allowedEvents) {
		ISpawnable spawn = Powerups.getRandomPowerupOrEvent(NoppaBot.this, allowedPowerups, allowedEvents);
		return scheduleSpawn(spawnTime, spawn);
	}
	
	/**
	 * Schedule a powerup/event spawn or spawn it immediately
	 * 
	 * @param spawnTime Spawn time or null for spawn immediately
	 */
	@Override
	public SpawnTask scheduleSpawn(Calendar spawnTime, ISpawnable spawn) {
		boolean immediateSpawn = (spawnTime == null);
		if (immediateSpawn) spawnTime = Calendar.getInstance();
		final String pattern = String.format("%d %d * * *", 
			spawnTime.get(Calendar.MINUTE), spawnTime.get(Calendar.HOUR_OF_DAY));

		SpawnTask spawnTask = new SpawnTask(spawnTime.getTime(), spawn);
		
		if (immediateSpawn) {
			spawnTask.execute(null);
		}
		else {
			spawnTask.id = scheduler.schedule(pattern, spawnTask);
			spawnTasks.add(spawnTask);
		}
		
		BasicPowerup powerup = spawnTask.getPowerup();
		if (powerup != null) { // Is BasicPowerup or Instant
			Calendar expireTime = (Calendar)spawnTime.clone();
			expireTime.add(Calendar.MINUTE, POWERUP_EXPIRE_MINUTES);
			ExpireTask expireTask = scheduleExpire(powerup, expireTime);
			spawnTask.expireTask = expireTask; // If we want to change the powerup, we want to alter the expire task too
			
			if (debug) System.out.printf("Spawning %s on %tR, expires on %tR\n", spawnTask.spawn, spawnTime.getTime(), expireTime.getTime());
		}
		else if (debug) { // Print events
			System.out.printf("Spawning %s on %tR\n", spawnTask.spawn, spawnTime.getTime());
		}
		
		return spawnTask;
	}


	@Override
	public ExpireTask scheduleExpire(Powerup powerup, Calendar expireTime) {
		final String expirePattern = String.format("%d %d * * *",
			expireTime.get(Calendar.MINUTE), expireTime.get(Calendar.HOUR_OF_DAY));
		
		ExpireTask expireTask = new ExpireTask(powerup);
		expireTask.id = scheduler.schedule(expirePattern, expireTask);
		expireTasks.add(expireTask);
		return expireTask;
	}
	
	private void clearPowerupTasks() {
		for (SpawnTask task : spawnTasks) {
			scheduler.deschedule(task.id);
		}
		spawnTasks.clear();
		
		for (ExpireTask task : expireTasks) {
			scheduler.deschedule(task.id);
		}
		expireTasks.clear();
	}
	
	private boolean isInRollPeriod() {
		if (debug) return true;
		
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

		synchronized (lock) {
			try {
				handleMessage(sender, message);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	private void handleMessage(String sender, String message) {
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
			rollAndParticipate(rollerNick, sides);
		}
		
		Matcher commandMatcher = commandAndArgument.matcher(message);
		if (commandMatcher.matches()) {
			String cmd = commandMatcher.group(1);
			String args = commandMatcher.group(2);
			String[] argsSplit = args == null ? new String[0] : args.split("\\s+");
			
			if (cmd.equalsIgnoreCase("grab") || cmd.equalsIgnoreCase("pick") 
				|| cmd.equalsIgnoreCase("take") || cmd.equalsIgnoreCase("get")) {
				grabPowerup(sender, args);
			}
			else if (cmd.equalsIgnoreCase("roll")) {
				if (argsSplit.length == 0) rollAndParticipate(sender, 100);
				else if (argsSplit.length == 1) rollAndParticipate(argsSplit[0], 100);
			}
			else if (cmd.equalsIgnoreCase("nextrolls")) {
				if (argsSplit.length == 0) listNextRolls(sender);
				else if (argsSplit.length == 1) listNextRolls(argsSplit[0]);
			}
			
			// No-arg commands
			else if (args == null) {
				if (cmd.equalsIgnoreCase("items")) {
					listItems(false);
				}
				else if (cmd.equalsIgnoreCase("look")) {
					listAvailablePowerups(sender);
				}
				else if (cmd.equalsIgnoreCase("rolls")) {
					listRolls();
				}
				else if (cmd.equalsIgnoreCase("peek")) {
					peekNextSpawns(sender);
				}
				else if (cmd.equalsIgnoreCase("autoroll")) {
					autorollFor(sender);
				}
				else if (cmd.equalsIgnoreCase("drop")) {
					dropPowerup(sender);
				}
				else if (cmd.equalsIgnoreCase("rules")) {
					listRules(sender);
				}
				else if (cmd.equalsIgnoreCase("cards") || cmd.equalsIgnoreCase("hands")) {
					pokerTable.listHands(false);
				}
				else if (cmd.equalsIgnoreCase("train")) {
					grabPowerup(sender, DicemonTrainer.NAME);
				}
				else if (cmd.equalsIgnoreCase("deal")) {
					grabPowerup(sender, PokerDealer.NAME);
				}
				else if (cmd.equalsIgnoreCase("reveal")) {
					Powerup powerup = powerups.get(sender);
					if (powerup != null && (powerup instanceof PokerHand || powerup instanceof BetterHand)) {
						rollAndParticipate(sender, 100);
					}
					else {
						sendChannelFormat("%s: you don't have a poker hand", Color.nick(sender));
					}
				}
			}
		}
	}

	private void dropPowerup(String nick) {
		Powerup powerup = powerups.get(nick);
		if (state != State.NORMAL) {
			sendChannelFormat("%s: you cannot drop items during the roll contest.", Color.nick(nick));
			return;
		}
		else if (powerup == null) {
			sendChannelFormat("%s: you have nothing to drop.", Color.nick(nick));
			return;
		}
		else if (!rules.canDropItems && !hasFavor(nick)) {
			return;
		}
		
		if (!rules.canDropItems) {
			favorsUsed.add(nick);
		}
		
		sendChannelFormat("%s drops the %s on the ground.", Color.nick(nick), 
			powerup.nameWithDetailsColored());
		powerups.remove(nick);
		availablePowerups.add(powerup);
		Calendar expireTime = Calendar.getInstance();
		expireTime.add(Calendar.MINUTE, POWERUP_EXPIRE_MINUTES);
		scheduleExpire(powerup, expireTime);
	}


	private void autorollFor(String nick) {
		if (autorolls.contains(nick)) {
			sendChannelFormat("%s: you've already requested me to autoroll for you.", Color.nick(nick));
			return;
		}
		else if (!hasFavor(nick)) return;
		
		favorsUsed.add(nick);
		sendChannelFormat("%s: ok, I will roll for you tonight.", Color.nick(nick));
		autorolls.add(nick);
	}


	private void peekNextSpawns(String nick) {
		if (!hasFavor(nick)) return;
		favorsUsed.add(nick);
		
		final int peekCount = 3;
		
		Iterable<SpawnTask> nextThree = Iterables.limit(spawnTasks, peekCount);
		String spawnInfo = StringUtils.join(nextThree, ", ", new StringConverter<SpawnTask>() {
			@Override
			public String toString(SpawnTask task) {
				String str = task.toStringColored();
				ISpawnable spawn = task.spawn;
				if (spawn != null && spawn instanceof Bomb) {
					str += " ... that item seems suspicious!";
				}
				return str;
			}
		});
		boolean moreToCome = spawnTasks.size() > peekCount;
		
		StringBuilder buf = new StringBuilder();
		if (spawnTasks.isEmpty()) {
			buf.append("No further events will occur today!");
		}
		else {
			buf.append("Next up is: ");
			buf.append(spawnInfo).append(". ");
			if (moreToCome) {
				buf.append("More events will still happen, though!");
			}
			else {
				buf.append("Those will be the last events for today.");
			}
		}
		
		sendAction(channel, String.format("whispers to %s what happens next!", nick));
		sendMessage(nick, buf.toString());
	}

	private boolean hasFavor(String nick) {
		if (favorsUsed.contains(nick)) {
			sendChannelFormat("%s: you have used your favor for today", Color.nick(nick));
			return false;
		}
		
		return true;
	}
	
	private void listItems(boolean isRollPeriodStart) {
		if (powerups.isEmpty() && !isRollPeriodStart) {
			sendChannel("Nobody has any items.");
			return;
		}
		
		if (!powerups.isEmpty() || !isRollPeriodStart) {
			StringBuilder buf = new StringBuilder();
			if (isRollPeriodStart) buf.append("Today you have collected: ");
			else buf.append("Items: ");
			boolean first = true;
			for (String nick : powerups.keySet()) {
				String powerupName = powerups.get(nick).nameWithDetailsColored();
				if (!first) buf.append(", ");
				if (!isRollPeriodStart) nick = Color.antiHilight(nick);
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

		List<Entry<String, Integer>> rollsList = rolls.orderedList();
		
		StringBuilder buf = new StringBuilder();
		buf.append("Rolls: ");
		boolean first = true;
		for (Entry<String, Integer> entry : rollsList) {
			String nick = entry.getKey();
			int roll = entry.getValue();
			if (!first) buf.append(", ");
			buf.append(String.format("%s %s(%s)", colorRoll(roll), offTargetInfo(roll), Color.antiHilight(nick)));
			first = false;
		}
		if (buf.length() > 0) sendChannel(buf.toString());
		else sendChannel("Nobody has rolled yet.");
	}
	
	private String offTargetInfo(int roll) {
		if (rules.winCondition == rules.ROLL_CLOSEST_TO_TARGET) {
			int rollTarget = rules.rollTarget;
			int dist = Math.abs(rollTarget - roll);
			return Color.custom("[" + dist + " off] ", Colors.WHITE);
		}
		else return "";
	}
	
	private void listAvailablePowerups(String nick) {
		if (availablePowerups.isEmpty()) {
			sendChannelFormat("%s: you see nothing of interest.", Color.nick(nick));
		}
		else {
			String items = StringUtils.joinColored(availablePowerups, ", ");
			sendChannelFormat("%s: You see: %s.", Color.nick(nick), items);
		}
	}
	
	private void listNextRolls(String nick) {
		Powerup powerup = powerups.get(nick);
		final int dieSides = powerup == null ? 100 : powerup.sides();
		PeekableRandom random = randoms.get(nick); // Don't create a new random if there isn't one
		
		if (random != null) {
			Map<Integer, Integer> nextRolls = random.getNextKnownRolls();
			if (!nextRolls.isEmpty()) {
				
				String predictions = StringUtils.join(nextRolls.entrySet(), ", ", new StringConverter<Map.Entry<Integer, Integer>>() {
					@Override
					public String toString(Map.Entry<Integer, Integer> pair) {
						int sides = pair.getKey();
						int roll = pair.getValue();
						String sidesStr = (sides == dieSides) ? Color.custom("d" + sides, Colors.WHITE) : ("d" + sides);
						return String.format("%s = %d", sidesStr, roll);
					};
				});
				sendChannelFormat("%s's predicted rolls are: %s.", Color.nick(nick), predictions);
				return;
			}
		}
		
		sendChannelFormat("No rolls are predicted for %s.", Color.nick(nick));
	}
	
	
	private void listRules(String nick) {
		String explanation = rules.getExplanation();
		sendChannelFormat("%s: %s", Color.nick(nick), explanation);
	}

	private void grabPowerup(String nick, String powerupName) {
		Powerup powerup = findPowerup(powerupName);

		// Allow grabbing items during roll period in debug mode
		if (state != State.NORMAL && !debug) {
			sendChannelFormat("%s: you cannot grab items during the roll contest.", Color.nick(nick));
		}
		else if (availablePowerups.isEmpty()) {
			sendChannelFormat("%s: nothing to grab.", Color.nick(nick));
		}
		else if (powerup != null && !powerup.canPickUp(nick, true)) {
			// canPickUp(*) will warn the user
		}
		else {
			if (powerup != null) {
				powerup.setOwner(nick);
				powerup.onPickup();
				if (powerup.isCarried()) {
					powerups.put(nick, powerup);
					availablePowerups.remove(powerup);
				}
				else if (powerup.isDestroyedAfterPickup()) {
					availablePowerups.remove(powerup);
				}
			}
			// User didn't specify which item to grab
			else if (powerupName == null) {
				String items = StringUtils.joinColored(availablePowerups, ", ");
				sendChannelFormat("%s: There are multiple items available: %s. Specify which one you want!", 
					Color.nick(nick), items);
			}
			// Unknown item
			else {
				sendChannelFormat("%s: There is no such item available.", Color.nick(nick));
			}
		}
	}
	
	private Powerup findPowerup(String name) {
		if (name == null) {
			if (availablePowerups.size() == 1) return availablePowerups.first();
			else return null;
		}
		
		for (Powerup powerup : availablePowerups) {
			String nameWithDetails = powerup.nameWithDetails();
			nameWithDetails = Colors.removeFormattingAndColors(nameWithDetails);
			if (nameWithDetails.toLowerCase().contains(name.toLowerCase())) return powerup;
		}
		
		return null;
	}
	
	private void rollAndParticipate(String nick, int sides) {
		boolean rollWillParticipate = rollWillParticipate(nick);
		
		if (sides == 100 && rollWillParticipate) {
			
			// Generate a roll
			int roll;
			if (powerups.containsKey(nick)) {
				// ... with the carried power-up
				Powerup powerup = powerups.get(nick);
				roll = powerup.onContestRoll();
			}
			else {
				// ... with the normal d100
				roll = doRoll(nick, sides);
			}
			
			// Activate power-ups which affect opponent rolls
			roll = fireEarlyOpponentRolled(nick, roll);

			// If win condition is non-standard, tell how this roll will be scored
			rules.winCondition.onContestRoll(this, nick, roll);

			// Participate the roll
			participate(nick, roll);
			
			// Activate power-ups which trigger effects when opponent rolled,
			// but don't modify opponent's roll
			fireLateOpponentRolled(nick, roll);
		}
		else {
			doRoll(nick, sides);
		}
	}
	
	private boolean rollWillParticipate(String nick) {
		boolean participated = participated(nick);
		boolean rollOrTiebreakPeriod = state == State.ROLL_PERIOD || state == State.SETTLE_TIE;
		return !participated && rollOrTiebreakPeriod;
	}
	
	@Override
	public int doRoll(String nick, int sides) {
		int roll = getRoll(nick, sides);
		if (sides == 100) {
			sendDefaultContestRollMessage(nick, roll, true, rollWillParticipate(nick));
		}
		else {
			sendChannelFormat("%s rolls %s!", Color.nick(nick), 
				Color.custom(String.valueOf(roll), Colors.WHITE));
		}
		return roll;
	}

	private int fireEarlyOpponentRolled(String nick, int roll) {
		// Early -- can modify the roll
		for (String powerupOwner : powerups.keySet()) {
			if (!powerupOwner.equals(nick)) {
				Powerup powerup = powerups.get(powerupOwner);
				roll = powerup.onOpponentRoll(nick, roll);
			}
		}
		return roll;
	}

	private void fireLateOpponentRolled(String nick, int roll) {
		// Late -- the roll is now determined, do something else
		for (String powerupOwner : powerups.keySet()) {
			if (!powerupOwner.equals(nick)) {
				Powerup powerup = powerups.get(powerupOwner);
				powerup.onOpponentRollLate(nick, roll);
			}
		}
	}


	@Override
	public void sendDefaultContestRollMessage(String nick, int roll, boolean colorNick, boolean colorRoll) {
		sendChannel(getDefaultContestRollMessage(nick, roll, colorNick, colorRoll));
	}

	@Override
	public String getDefaultContestRollMessage(String nick, int roll, boolean colorNick, boolean colorRoll) {
		String participatedMsg = participated(nick) ? 
			" You've already rolled " + participatingRoll(nick) + " though, this roll won't participate!" : "";
		String rollStr = rollToString(roll, colorRoll);
		String nickColored = colorNick ? Color.nick(nick) : nick;
		return String.format("%s rolls %s! %s%s", nickColored, rollStr, grade(roll), participatedMsg);
	}
	
	@Override
	public String grade(int roll) {
		int score = rules.winCondition.assignScore(roll);
		if (score >= 100) return "You showed us....the ULTIMATE roll!";
		else if (score >= 95) return "You are a super roller!";
		else if (score >= 90) return "Amazing!";
		else if (score >= 80) return "Nicely done!";
		else if (score >= 70) return "Quite good!";
		else if (score >= 60) return "That's ok!";
		else if (score >= 50) return "... That's decent!";
		else if (score >= 40) return "... Could've gone better!";
		else if (score >= 30) return "That's kind of a low roll!";
		else if (score >= 20) return "Not having much luck today? :(";
		else if (score >= 10) return "Still at two digits!";
		else if (score > 1) return "Seek some advice from your local qualified rolling professional!";
		else if (score == 1) return "Yikes!";
		else return "Huh?";
	}
	
	@Override
	public void participate(String nick, int rollValue) {
		if (state == State.ROLL_PERIOD && !rolls.participated(nick)) {
			rolls.put(nick, rollValue);
			if (isOvertime()) tryEndRollPeriod();
		}
		else if (state == State.SETTLE_TIE && tiebreakers.contains(nick)) {
			if (!rolls.participated(nick)) {
				rolls.put(nick, rollValue);
				tiebreakers.remove(nick);
				if (tiebreakers.isEmpty()) tryEndRollPeriod();
				else {
					String pendingTiebreakers = StringUtils.join(tiebreakers, ", ");
					sendChannelFormat("I still need a roll from %s.", pendingTiebreakers);
				}
			}
		}
	}
	
	@Override
	public boolean participated(String nick) {
		return rolls.participated(nick);
	}
	
	private int participatingRoll(String nick) {
		return rolls.get(nick);
	}
	

	private void doTasksBeforeRollPeriodStart() {
		expireAllPowerups();
		clearPowerupTasks();

		listItems(true);
	}
	
	private void startRollPeriod() {
		state = State.ROLL_PERIOD;
		
		// Do this again, because doTasksBeforeRollPeriodStart() isn't guaranteed to occur
		clearPowerupTasks();

		sendChannel(randomRollStartMsg());
		rules.onRollPeriodStart();
		if (pokerTable.gameStarted) pokerTable.tryRevealTableCards(true);
		
		for (String nick : powerups.keySet()) {
			Powerup powerup = powerups.get(nick);
			powerup.onRollPeriodStart();
		}
	}

	private void autorollFor(Set<String> autorolls) {
		
		// Filter people who have rolled
		Set<String> rollers = new TreeSet<String>(autorolls);
		rollers.removeAll(rolls.participants());
		
		if (!rollers.isEmpty()) {
			sendChannelFormat("I'll roll for: %s", StringUtils.join(rollers, ", "));
			
			for (String nick : rollers) {
				rollAndParticipate(nick, 100);
			}
		}
	}
	
	private void tryEndRollPeriod() {
		
		if (rolls.isEmpty()) {
			sendChannel("No rolls within 10 minutes. Now first roll wins, be quick!");
			state = State.ROLL_PERIOD;
			return;
		}
		else {
			List<String> winningRollers = getWinningRollers();
			if (winningRollers.size() == 1) {
				endRollPeriod(winningRollers);
			}
			else {
				startSettleTie(winningRollers);
			}
		}
	}

	private void startSettleTie(List<String> winningRollers) {
		// Deschedule previous settle tie timeout, if there is one
		if (settleTieTimeoutTask != null) {
			scheduler.deschedule(settleTieTimeoutTask.id);
			settleTieTimeoutTask = null;
		}
		
		lastTiebreakPeriodStartTime = Calendar.getInstance();
		String tiebreakersStr = StringUtils.join(winningRollers, ", ");
		int roll = rolls.get(winningRollers.get(0));
		String msg = String.format(
			"The roll %d was tied between %s. Roll again within 10 minutes to settle the score!", 
			roll, tiebreakersStr);
		sendChannel(msg);
		tiebreakers.addAll(winningRollers);
		
		rolls.clear();
		
		for (String tiebreakerNick : tiebreakers) {
			Powerup powerup = powerups.get(tiebreakerNick);
			if (powerup != null) powerup.onTiebreakPeriodStart();
		}
		
		settleTieTimeoutTask = scheduleSettleTieTimeout();
		
		state = State.SETTLE_TIE;

		// Schedule autoroll for tiebreakers
		Set<String> tiebreakerAutorolls = new TreeSet<String>();
		tiebreakerAutorolls.addAll(tiebreakers);
		tiebreakerAutorolls.retainAll(autorolls);
		scheduleSettleTieAutorolls(tiebreakerAutorolls);
	}


	private SettleTieTimeoutTask scheduleSettleTieTimeout() {
		SettleTieTimeoutTask timeoutTask = new SettleTieTimeoutTask();
		Calendar time = (Calendar)lastTiebreakPeriodStartTime.clone();
		time.add(Calendar.MINUTE, 10);
		final String pattern = String.format("%d %d * * *", time.get(Calendar.MINUTE), time.get(Calendar.HOUR_OF_DAY));
		timeoutTask.id = scheduler.schedule(pattern, timeoutTask);
		return timeoutTask;
	}
	
	private class SettleTieTimeoutTask extends Task {
		public String id;
		
		@Override
		public void execute(TaskExecutionContext arg0) throws RuntimeException {
			synchronized (lock) {
				if (state == State.SETTLE_TIE) {
					sendChannel("I'm calling this now...");
					tiebreakers.clear();
					tryEndRollPeriod();
				}
				scheduler.deschedule(id);
			}
		}
	}
	
	private void scheduleSettleTieAutorolls(Set<String> autorolls) {
		SettleTieAutorollsTask autorollTask = new SettleTieAutorollsTask(autorolls);
		Calendar time = (Calendar)lastTiebreakPeriodStartTime.clone();
		time.add(Calendar.MINUTE, 5);
		final String pattern = String.format("%d %d * * *", time.get(Calendar.MINUTE), time.get(Calendar.HOUR_OF_DAY));
		autorollTask.id = scheduler.schedule(pattern, autorollTask);
	}
	
	private class SettleTieAutorollsTask extends Task {
		public String id;
		private Set<String> autorolls;
		
		public SettleTieAutorollsTask(Set<String> autorolls) {
			this.autorolls = autorolls;
		}
		
		@Override
		public void execute(TaskExecutionContext arg0) throws RuntimeException {
			synchronized (lock) {
				if (state == State.SETTLE_TIE) {
					autorollFor(autorolls);
				}
				scheduler.deschedule(id);
			}
		}
	}
	
	private void endRollPeriod(List<String> winningRollers) {
		String winner = winningRollers.get(0);
		int roll = rolls.get(winner);
		String msg = String.format(randomRollEndMsg(), Color.nick(winner), colorRoll(roll));
		sendChannel(msg);
		
		updateRecords(winner);
		
		expireAllPowerups(); // Should have none on the ground now, but just to be safe
		rolls.clear();
		tiebreakers.clear();
		powerups.clear();
		favorsUsed.clear();
		autorolls.clear();
		pokerTable.clear();
		schedulePowerupsOfTheDay();
		
		if (rules.reset()) {
			sendChannel("The non-standard rules return to normal.");
		}
		
		state = State.NORMAL;
	}

	
	private void updateRecords(String winner) {
		RollRecords rec = loadRollRecords();
		if (rec == null) return;
		
		// Ensure record contains every user
		for (String nick : rolls.participants()) {
			rec.getOrAddUser(nick);
		}
		
		// Increment wins for the winner
		rec.incrementWins(winner);
		
		// Add the today's roll for everyone
		for (User user : rec.users) {
			if (rolls.participated(user.nick)) {
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
	public Rolls getRolls() {
		return rolls;
	}
	
	private PeekableRandom getRandomFor(String nick) {
		if (!randoms.containsKey(nick)) randoms.put(nick, new PeekableRandom());
		return randoms.get(nick);
	}
	
	@Override
	public int getRoll(String nick, int sides) {
		PeekableRandom random = getRandomFor(nick);
		return random.nextRoll(sides);
	}
	
	@Override
	public int peekRoll(String nick, int sides, boolean makeItKnown) {
		PeekableRandom random = getRandomFor(nick);
		int roll = random.peek(sides);
		if (makeItKnown) random.setKnown(sides, true); // Don't hide already known rolls
		return roll;
	}
	
	
	@Override
	public void setNextRoll(String nick, int sides, int roll) {
		PeekableRandom random = getRandomFor(nick);
		random.setNextRoll(sides, roll);
	}
	
	public int countPowerups(Class<? extends Powerup> type) {
		int count = 0;
		for (Powerup powerup : powerups.values()) {
			if (type.isAssignableFrom(powerup.getClass())) count++;
		}
		return count;
	}
	
	public List<String> getWinningRollers() {
		return rolls.getWinningRollers();
	}
	
	@Override
	public int getSecondsAfterPeriodStart() {
		long now = Calendar.getInstance().getTimeInMillis();
		Calendar periodStart;
		if (state == State.ROLL_PERIOD) periodStart = rollPeriodStartTime;
		else if (state == State.SETTLE_TIE) periodStart = lastTiebreakPeriodStartTime;
		else throw new IllegalStateException("Not in roll or tie period");
		long passed = now - periodStart.getTimeInMillis();
		long secondsPassed = passed / 1000;
		return (int)secondsPassed;
	}
	
	@Override
	public String remainingSpawnsInfo() {
		String info = StringUtils.joinColored(spawnTasks, ", ");
		return info;
	}
	
	@Override
	public Calendar getRollPeriodStartTime() {
		return rollPeriodStartTime;
	}
	
	
	@Override
	public Calendar getSpawnEndTime() {
		return spawnEndTime;
	}
	
	@Override
	public Rules getRules() {
		return rules;
	}
	
	@Override
	public void onRulesChanged() {
		rolls.onRulesChanged();
	}
	
	@Override
	public void clearFavorsUsed() {
		favorsUsed.clear();
	}
	
	/**
	 * Randomly overwrite some scheduled spawns with apprentice dice spawns
	 */
	@Override
	public void insertApprenticeDice() {
		for (SpawnTask task : spawnTasks) {
			if (task.getPowerup() != null && commonRandom.nextFloat() < 0.15f) {
				ApprenticeDie apprenticeDie = new ApprenticeDie();
				apprenticeDie.initialize(this);
				task.spawn = apprenticeDie;
				task.expireTask.powerup = apprenticeDie;
			}
		}
	}
	
	@Override
	public List<String> getRandomPowerupOwners() {
		ArrayList<String> owners = new ArrayList<String>(getPowerups().keySet());
		Collections.shuffle(owners);
		if (owners.isEmpty()) return owners;
		else {
			int removeCount = commonRandom.nextInt(owners.size());
			return owners.subList(0, owners.size() - removeCount);
		}
	}
	
	public String colorRoll(int roll) {
		if (rolls.isWinningRoll(roll)) return Color.winningRoll(roll);
		else return Color.losingRoll(roll);
	}
	
	@Override
	public String rollToString(int roll) {
		return rollToString(roll, true);
	}
	
	@Override
	public String rollToString(int roll, boolean colorRoll) {
		if (roll <= 100 && roll >= 0) {
			return maybeColorRoll(roll, colorRoll);
		}
		else {
			if (rules.cappedRolls) {
				return String.format("%d (= %s)", roll, maybeColorRoll(clampRoll(roll), colorRoll));
			}
			else {
				return maybeColorRoll(roll, colorRoll);
			}
		}
	}
	
	private String maybeColorRoll(int roll, boolean colorRoll) {
		if (colorRoll) return colorRoll(roll); // Color it green/red
		else return Color.emphasize(roll); // Color it hilighted white
	}
	
//	private void revealPokerTableCards() {
//		boolean cardsFound = false;
//		for (Powerup powerup : powerups.values()) {
//			if (powerup instanceof PokerCards || powerup instanceof PocketAces) {
//				cardsFound = true;
//				break;
//			}
//		}
//		if (!cardsFound) return;
//		
//		createPokerTableCards();
//		
//		sendChannelFormat("I'll deal the table cards for poker players... %s", pokerTableCards.toString());
//	}

//	private void createPokerTableCards() {
//		Deck deck = new Deck(System.currentTimeMillis());
//		deck.shuffle();
//		pokerTableCards = new Hand();
//		for (int i = 0; i < 5; i++) {
//			Card card = deck.deal();
//			pokerTableCards.addCard(card);
//		}
//		pokerTableCards.sort();
//	}
	
	@Override
	public int clampRoll(int roll) {
		if (rules.cappedRolls) return Math.max(0, Math.min(100, roll));
		else return roll;
	}
	
	@Override
	public int getPowerupSides(String nick) {
		Powerup powerup = powerups.get(nick);
		if (powerup == null) return 100;
		else return powerup.sides();
	}
	
	@Override
	public Scheduler getScheduler() {
		return scheduler;
	}
	
	@Override
	public Object getLock() {
		return lock;
	}
	
	@Override
	public PokerTable getPokerTable() {
		return pokerTable;
	}
	
	@Override
	public State getState() {
		return state;
	}

	private boolean isOnChannel(String nick) {
		org.jibble.pircbot.User[] users = getUsers(channel);
		for (org.jibble.pircbot.User user : users) {
			if (user.getNick().equals(nick)) return true;
		}
		return false;
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
