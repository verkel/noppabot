/*
 * Created on 21.1.2014
 * @author verkel
 */
package noppabot.spawns;

import java.util.Comparator;

import noppabot.MathUtils;


public class SpawnableComparator implements Comparator<ISpawnable> {

	@Override
	public int compare(ISpawnable a, ISpawnable b) {
		int aType = typeOrdinal(a);
		int bType = typeOrdinal(b);
		
		int typeCmp = MathUtils.compare(aType, bType);
		if (typeCmp != 0) return typeCmp;
		
		int nameCmp = a.nameWithDetails().compareTo(b.nameWithDetails());
		if (nameCmp != 0) return nameCmp;
		
		return a.hashCode() - b.hashCode();
	}
	
	private int typeOrdinal(ISpawnable s) {
		if (s instanceof Event) return 0;
		if (s instanceof Instant) return 1;
		if (s instanceof BasicPowerup) return 2;
		if (s instanceof EvolvedPowerup) return 3;
		
		throw new IllegalArgumentException();
	}
}
