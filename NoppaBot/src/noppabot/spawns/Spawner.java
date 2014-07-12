/*
 * Created on 10.12.2013
 * @author verkel
 */
package noppabot.spawns;

import java.util.*;

public class Spawner<S extends ISpawnable> implements Iterable<S> {
	private NavigableMap<Float, S> chances = new TreeMap<Float, S>();
	private Random random = new Random();
	private LastSpawn<S> lastSpawn;
	private boolean clone = true;

	/**
	 * Allow subsequent same spawns
	 */
	public Spawner(Collection<S> spawnables) {
		this(spawnables, Spawner.<S>allowSameSpawns());
	}

	/**
	 * Disallow subsequent same spawns. Share the lastSpawn object with spawners
	 * you wish to combine the last spawn tracking with.
	 */
	public Spawner(Collection<S> spawnables, LastSpawn<S> lastSpawn) {
		this.lastSpawn = lastSpawn;
		
		// Compute sum of spawn chances
		float sum = 0f;
		for (S s : spawnables) {
			sum += s.spawnChance();
		}
		
		// Build the probability distribution
		float chanceCeil = 0f;
		for (S s : spawnables) {
			chanceCeil += s.spawnChance() / sum;
			chances.put(chanceCeil, s);
		}
	}
	
	public S spawn() {
		// Prevent two same spawns in a row
		float rnd, key;
		S value;
		do { 
			rnd = random.nextFloat();
			key = chances.higherKey(rnd);
			value = chances.get(key);
		}
		while (lastSpawn.isSame(value));
		lastSpawn.setValue(value);
		
		return clone(value);
	}
	
	@Override
	public Iterator<S> iterator() {
		return new Iterator<S>() {
			private Iterator<S> it = chances.values().iterator();

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public S next() {
				return Spawner.this.clone(it.next());
			}

			@Override
			public void remove() {
			}
			
		};
	}

	public boolean isClone() {
		return clone;
	}

	public void setClone(boolean clone) {
		this.clone = clone;
	}
	
	@SuppressWarnings("unchecked")
	private S clone(S obj) {
		if (!clone) return obj;
		try {
			obj = (S)obj.getClass().newInstance();
			return obj;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	// So that we may share the lastSpawn between multiple spawners
	public static class LastSpawn<S> {
		private S value = null;

		public S getValue() {
			return value;
		}
		
		public boolean isSame(S newValue) {
			if (value == null) return false;
			return newValue.equals(value);
		}

		public void setValue(S value) {
			this.value = value;
		}
	}
	
	public static <S> LastSpawn<S> allowSameSpawns() {
		return new LastSpawn<S>() {
			@Override
			public void setValue(S value) {
			}
		};
	}
}
