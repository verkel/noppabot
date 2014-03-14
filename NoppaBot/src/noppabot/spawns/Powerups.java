/*
 * Created on 27.10.2013
 * @author verkel
 */
package noppabot.spawns;

import java.util.*;

import noppabot.INoppaBot;
import noppabot.spawns.Spawner.LastSpawn;
import noppabot.spawns.dice.*;
import noppabot.spawns.events.*;
import noppabot.spawns.instants.*;

public class Powerups {

	public static Random powerupRnd = new Random();

	public static final Spawner<BasicPowerup> allPowerups;
	public static final Spawner<BasicPowerup> firstPowerup;
	public static final Spawner<BasicPowerup> diceStormPowerups;
	public static final Spawner<BasicPowerup> diceBrosPowerups;
	public static final Spawner<Event> allEvents;
	public static final Spawner<Event> allEventsMinusFourthWall;
	public static final Spawner<Event> lateEvents;
	
	static {
		List<BasicPowerup> allPowerupsList = new ArrayList<BasicPowerup>();
		List<BasicPowerup> firstPowerupList = new ArrayList<BasicPowerup>();
		List<BasicPowerup> diceStormPowerupsList = new ArrayList<BasicPowerup>();
		List<BasicPowerup> diceBrosPowerupsList = new ArrayList<BasicPowerup>();
		List<Event> allEventsList = new ArrayList<Event>();
		List<Event> allEventsMinusFourthWallList = new ArrayList<Event>();
		List<Event> lateEventsList = new ArrayList<Event>();
		
		/*
		 * Instances are used here because some metadata is stored in instance
		 * getters. Namely the spawn chance. Because static methods cannot be
		 * specified in interfaces, you'd need to have this data as annotations to
		 * be accessible without instantiating...
		 */
		
		// Apprentice die is not put here, but can be spawned regardless if there are master dies
		allPowerupsList.addAll(Arrays.asList(new BagOfDice(), new DicemonTrainer(), new DiceBros(),
			new DicePirate(), new DiceRecycler(), new Diceteller(), new EnchantedDie(), new ExtremeDie(), new FastDie(),
			new ImitatorDie(), new HumongousDie(), new LuckyDie(), new MasterDie(), new PolishedDie(), new PrimalDie(),
			new RollingProfessional(), new SteadyDie(), new TrollingProfessional(), new VariableDie(), new WeightedDie()));

		diceBrosPowerupsList.addAll(Arrays.asList(new BagOfDice(), new EnchantedDie(), new FastDie(),
			new ExtremeDie(), new LuckyDie(), new MasterDie(), new PolishedDie(), new PrimalDie(),
			new SteadyDie(), new VariableDie(), new WeightedDie()));

		firstPowerupList.addAll(allPowerupsList);
		firstPowerupList.removeAll(Arrays.asList(new DicemonTrainer(), new DicePirate(), new DiceRecycler()));

		diceStormPowerupsList.addAll(allPowerupsList);
		diceStormPowerupsList.removeAll(Arrays.asList(new DicePirate()));
		
		allEventsList.addAll(Arrays.asList(new DiceMutation(), new DiceStorm(), 
			new FourthWallBreaks(), new FavorRefresh(), new RulesChange()));
		
		allEventsMinusFourthWallList.addAll(allEventsList);
		allEventsMinusFourthWallList.remove(new FourthWallBreaks());
		
		lateEventsList.addAll(allEventsList);
		lateEventsList.removeAll(Arrays.asList(new FourthWallBreaks()));
		
		LastSpawn<BasicPowerup> lastPowerup = new LastSpawn<BasicPowerup>();
		LastSpawn<Event> lastEvent = new LastSpawn<Event>();
		
		allPowerups = new Spawner<BasicPowerup>(allPowerupsList, lastPowerup);
		firstPowerup = new Spawner<BasicPowerup>(firstPowerupList, lastPowerup);
		diceStormPowerups = new Spawner<BasicPowerup>(diceStormPowerupsList, lastPowerup);
		diceBrosPowerups = new Spawner<BasicPowerup>(diceBrosPowerupsList, new LastSpawn<BasicPowerup>());
		allEvents = new Spawner<Event>(allEventsList, lastEvent);
		allEventsMinusFourthWall = new Spawner<Event>(allEventsMinusFourthWallList, lastEvent);
		lateEvents = new Spawner<Event>(lateEventsList, lastEvent);
	}

	public static ISpawnable getRandomPowerupOrEvent(INoppaBot bot, Spawner<BasicPowerup> spawnPowerups, Spawner<Event> spawnEvents) {
		if (spawnEvents != null && powerupRnd.nextFloat() < 0.10f) {
			return getRandomEvent(spawnEvents);
		}
		else {
			return getRandomPowerup(bot, spawnPowerups);
		}
	}
	
	public static BasicPowerup getRandomPowerup(INoppaBot bot, Spawner<BasicPowerup> spawnPowerups) {
		BasicPowerup powerup = spawnPowerups.spawn();
		powerup.initialize(bot);
		return powerup;
	}
	
	public static Event getRandomEvent(Spawner<Event> spawnEvents) {
		Event event = spawnEvents.spawn();
		return event;
	}
}
