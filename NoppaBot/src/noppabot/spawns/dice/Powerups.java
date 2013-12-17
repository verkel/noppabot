/*
 * Created on 27.10.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import java.util.*;

import noppabot.INoppaBot;
import noppabot.spawns.*;
import noppabot.spawns.Spawner.LastSpawn;
import noppabot.spawns.events.*;
import noppabot.spawns.instants.*;

public class Powerups {

	public static Random powerupRnd = new Random();

	public static final Spawner<Powerup> allPowerups;
	public static final Spawner<Powerup> firstPowerup;
	public static final Spawner<Powerup> diceStormPowerups;
	public static final Spawner<Powerup> diceBrosPowerups;
	public static final Spawner<Event> allEvents;
	public static final Spawner<Event> allEventsMinusFourthWall;
	
	static {
		List<Powerup> allPowerupsList = new ArrayList<Powerup>();
		List<Powerup> firstPowerupList = new ArrayList<Powerup>();
		List<Powerup> diceStormPowerupsList = new ArrayList<Powerup>();
		List<Powerup> diceBrosPowerupsList = new ArrayList<Powerup>();
		List<Event> allEventsList = new ArrayList<Event>();
		List<Event> allEventsMinusFourthWallList = new ArrayList<Event>();
		
		// Apprentice die is not put here, but can be spawned regardless if there are master dies
		allPowerupsList.addAll(Arrays.asList(new BagOfDice(), new DicemonTrainer(), new DiceBros(),
			new DicePirate(), new DiceRecycler(), new Diceteller(), new EnchantedDie(), new ExtremeDie(), new FastDie(),
			new GroundhogDie(), new HumongousDie(), new LuckyDie(), new MasterDie(), new PolishedDie(), new PrimalDie(),
			new RollingProfessional(), new TrollingProfessional(), new VariableDie(), new WeightedDie()));

		diceBrosPowerupsList.addAll(Arrays.asList(new BagOfDice(), new EnchantedDie(),
			new ExtremeDie(), new LuckyDie(), new MasterDie(), new PolishedDie(), new PrimalDie(),
			new RollingProfessional(), new WeightedDie()));

		firstPowerupList.addAll(allPowerupsList);
		firstPowerupList.removeAll(Arrays.asList(new DicemonTrainer(), new DicePirate(), new DiceRecycler()));

		diceStormPowerupsList.addAll(allPowerupsList);
		diceStormPowerupsList.removeAll(Arrays.asList(new DicePirate()));
		
		allEventsList.addAll(Arrays.asList(new DiceMutation(), new DiceStorm(), 
			new FourthWallBreaks(), new RulesChange()));
		
		allEventsMinusFourthWallList.addAll(allEventsList);
		allEventsMinusFourthWallList.remove(new FourthWallBreaks());
		
		LastSpawn lastPowerup = new LastSpawn();
		LastSpawn lastEvent = new LastSpawn();
		
		allPowerups = new Spawner<Powerup>(allPowerupsList, lastPowerup);
		firstPowerup = new Spawner<Powerup>(firstPowerupList, lastPowerup);
		diceStormPowerups = new Spawner<Powerup>(diceStormPowerupsList, lastPowerup);
		diceBrosPowerups = new Spawner<Powerup>(diceBrosPowerupsList, new LastSpawn());
		allEvents = new Spawner<Event>(allEventsList, lastEvent);
		allEventsMinusFourthWall = new Spawner<Event>(allEventsMinusFourthWallList, lastEvent);
	}

	public static ISpawnable getRandomPowerupOrEvent(INoppaBot bot, Spawner<Powerup> spawnPowerups, Spawner<Event> spawnEvents) {
		if (spawnEvents != null && powerupRnd.nextFloat() < 0.08f) {
			return getRandomEvent(spawnEvents);
		}
		else {
			return getRandomPowerup(bot, spawnPowerups);
		}
	}
	
	public static Powerup getRandomPowerup(INoppaBot bot, Spawner<Powerup> spawnPowerups) {
		Powerup powerup = spawnPowerups.spawn();
		powerup.initialize(bot);
		return powerup;
	}
	
	public static Event getRandomEvent(Spawner<Event> spawnEvents) {
		Event event = spawnEvents.spawn();
		return event;
	}
	
	// DupedDie -- choose the better one of two rolls
}
