/*
 * Created on 10.12.2013
 * @author verkel
 */
package noppabot.spawns;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

public class Spawner<S extends ISpawnable> implements Iterable<S> {
	private NavigableMap<Double, SpawnInfo<S>> chances = new TreeMap<Double, SpawnInfo<S>>();
	private Random random = new Random();
	private LastSpawn<S> lastSpawn;
	private String description;
//	private boolean clone = true;

	/**
	 * Make a new spawner record
	 */
//	public static <T> SpawnInfo<T> spawn(Supplier<T> supplier, double spawnChance) {
//		return new SpawnInfo<T>(supplier, spawnChance);
//	}
	
	/**
	 * Record of something the spawner can spawn
	 */
	public static interface SpawnInfo<T> {
		public default double spawnChance() {
			return 1.0;
		}
		
		public T create();
	}

	// Can this even be a constructor?
	public static <T extends ISpawnable, I extends SpawnInfo<T>> Spawner<T> create(
		List<I> spawnInfos, LastSpawn<T> lastSpawn, Predicate<I> filter) {

		// We necessarily iterate over the stream two times, so we have to recreate it
		Supplier<Stream<? extends SpawnInfo<T>>> stream = () -> spawnInfos.stream().filter(filter);
		return new Spawner<T>(stream, lastSpawn);
	}
	
	@SafeVarargs
	public static <T extends ISpawnable, I extends SpawnInfo<T>> Spawner<T> create(I... elems) {
		return new Spawner<T>(() -> Arrays.stream(elems));
	}
	
	/**
	 * Allow subsequent same spawns
	 */
	public Spawner(Supplier<Stream<? extends SpawnInfo<S>>> spawnables) {
		this(spawnables, Spawner.<S>allowSameSpawns());
	}

	/**
	 * Disallow subsequent same spawns. Share the lastSpawn object with spawners
	 * you wish to combine the last spawn tracking with.
	 */
	public Spawner(Supplier<Stream<? extends SpawnInfo<S>>> spawnables, LastSpawn<S> lastSpawn) {
		this.lastSpawn = lastSpawn;
		
		// Compute sum of spawn chances
		double sum = spawnables.get().mapToDouble(s -> s.spawnChance()).sum();
		
		// Build the probability distribution
		double chanceCeil = 0f;
		for (Iterator<? extends SpawnInfo<S>> it = spawnables.get().iterator(); it.hasNext();) {
			SpawnInfo<S> s = it.next();
			chanceCeil += s.spawnChance() / sum;
			chances.put(chanceCeil, s);
		}
	}
	
	public S spawn() {
		// Prevent two same spawns in a row
		double rnd, key;
		SpawnInfo<S> spawnInfo;
		do { 
			rnd = random.nextFloat();
			key = chances.higherKey(rnd);
			spawnInfo = chances.get(key);
		}
		while (lastSpawn.isSame(spawnInfo));
		lastSpawn.setValue(spawnInfo);
		
		return spawnInfo.create(); //clone(value);
	}
	
	public boolean canSpawn(Predicate<SpawnInfo> predicate) {
		return chances.values().stream().anyMatch(predicate);
	}
	
	/**
	 * Create a spawner based on this spawner with spawn infos that do not
	 * satisfy given predicate filtered out
	 */
	public Spawner<S> subSpawner(Predicate<SpawnInfo> predicate, LastSpawn<S> lastSpawn) {
		Supplier<Stream<? extends SpawnInfo<S>>> stream = () -> chances.values().stream()
			.filter(predicate);
		return new Spawner<S>(stream, lastSpawn);
	}
	
	/**
	 * Iterating over this spawner will spawn every value registered to it
	 */
	@Override
	public Iterator<S> iterator() {
		return new Iterator<S>() {
			private Iterator<SpawnInfo<S>> it = chances.values().iterator();

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public S next() {
				return it.next().create();
			}

			@Override
			public void remove() {
			}
			
		};
	}

	// So that we may share the lastSpawn between multiple spawners
	public static class LastSpawn<S> {
		private SpawnInfo<S> lastInfo = null;

		public SpawnInfo<S> getValue() {
			return lastInfo;
		}
		
		public boolean isSame(SpawnInfo<S> newInfo) {
			if (lastInfo == null) return false;
			return newInfo.equals(lastInfo);
		}

		public void setValue(SpawnInfo<S> value) {
			this.lastInfo = value;
		}
	}
	
	public static <S> LastSpawn<S> allowSameSpawns() {
		return new LastSpawn<S>() {
			@Override
			public void setValue(SpawnInfo<S> value) {
			}
		};
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
