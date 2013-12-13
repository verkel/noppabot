/*
 * Created on 10.12.2013
 * @author verkel
 */
package noppabot.spawns;

import java.util.*;



public class Spawner<S extends ISpawnable> {
	private NavigableMap<Float, S> chances = new TreeMap<Float, S>();
	private Random random = new Random();
	private LastSpawn lastSpawn;
	
	public Spawner(Collection<S> spawnables, LastSpawn lastSpawn) {
		this.lastSpawn = lastSpawn;
		
		// Compute sum of spawn chances
		float sum = 0f;
		for (S s : spawnables) {
			sum += s.getSpawnChance();
		}
		
		// Build the probability distribution
		float chanceCeil = 0f;
		for (S s : spawnables) {
			chanceCeil += s.getSpawnChance() / sum;
			chances.put(chanceCeil, s);
		}
	}
	
	public S spawn() {
		// Prevent two same spawns in a row
		float rnd, key;
		do { rnd = random.nextFloat(); }
		while ((key = chances.higherKey(rnd)) == lastSpawn.key);
		lastSpawn.key = key;
		
		S value = chances.get(key);
		return clone(value);
	}
	
	@SuppressWarnings("unchecked")
	private S clone(S obj) {
		try {
			obj = (S)obj.getClass().newInstance();
			return obj;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	// So that we may share the lastSpawn between multiple spawners
	public static class LastSpawn {
		public float key = -1f;
	}
}
