/*
 * Created on 5.8.2014
 * @author verkel
 */
package noppabot.spawns;

/**
 * Record of something the spawner can spawn
 */
public interface SpawnInfo<T> {
	public default double spawnChance() {
		return 1.0;
	}
	
	public T create();
	
	public default SpawnInfo<T> withChance(double spawnChance) {
		return new SpawnInfo<T>() {

			@Override
			public T create() {
				return this.create();
			}
			
			@Override
			public double spawnChance() {
				return spawnChance;
			}
		};
	}
}