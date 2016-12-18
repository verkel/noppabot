/*
 * Created on 18.12.2016
 * @author verkel
 */
package noppabot.spawns.instants;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.*;

import noppabot.spawns.instants.Present.PresentSpawnInfo;

import org.junit.Test;

public class TestPresent {

	private static final double EPS = 0.00001;

	@Test
	public void spawnChance_shouldBeElevatedOnChristmasTime() {
		int[] elevatedDays = { 21, 22, 23, 24 };
		for (int elevatedDay : elevatedDays) {
			assertDaysSpawnChance(elevatedDay, true);
		}
		int[] normalDays = { 1, 20, 25 };
		for (int normalDay : normalDays) {
			assertDaysSpawnChance(normalDay, false);
		}
	}

	private void assertDaysSpawnChance(int elevatedDay, boolean elevated) {
		PresentSpawnInfo spawnInfo = spy(Present.info);
		when(spawnInfo.currentDate()).thenReturn(decemberDay(elevatedDay));
		if (elevated) assertEquals(Present.CHRISTMAS_SPAWN_CHANCE, spawnInfo.spawnChance(), EPS);
		else assertNotEquals(Present.CHRISTMAS_SPAWN_CHANCE, spawnInfo.spawnChance(), EPS);
	}

	private LocalDate decemberDay(int dayOfMonth) {
		return LocalDate.of(Year.now().getValue(), Month.DECEMBER, dayOfMonth);
	}

}
