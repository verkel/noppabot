/*
 * Created on 18.12.2016
 * @author verkel
 */
package noppabot.spawns;

import static org.junit.Assert.*;
import noppabot.spawns.*;

import org.junit.Test;


public class TestSpawner {

	@Test
	public void rebuildSpawnChances_shouldAlterSpawnChances() {
		boolean[] spawnA = { true };
		ISpawnable itemA = new Spawnable("A"), itemB = new Spawnable("B");
		
		SpawnInfo a = new SpawnInfo() {

			@Override
			public Object create() {
				return itemA;
			}
			
			@Override
			public double spawnChance() {
				return spawnA[0] ? 1 : 0;
			}
		};

		SpawnInfo b = new SpawnInfo() {

			@Override
			public Object create() {
				return itemB;
			}
			
			@Override
			public double spawnChance() {
				return spawnA[0] ? 0 : 1;
			}
		};
		
		@SuppressWarnings("unchecked")
		Spawner spawner = Spawner.create(a, b);

		// Spawn should not change if spawner is not rebuilt
		spawnA[0] = true;
		assertEquals(itemA, spawner.spawn());
		spawnA[0] = false;
		assertEquals(itemA, spawner.spawn());
		
		// When spawner is rebuilt, spawn should change
		spawnA[0] = true;
		assertEquals(itemA, spawner.spawn());
		spawnA[0] = false; spawner.rebuildSpawnChances();
		assertEquals(itemB, spawner.spawn());
	}

	private class Spawnable implements ISpawnable {

		private String name;

		public Spawnable(String name) {
			this.name = name;
		}
		
		@Override
		public String toStringColored() {
			return null;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public String nameWithDetails() {
			return null;
		}
		
	}
}
