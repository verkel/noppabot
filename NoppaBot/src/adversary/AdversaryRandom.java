/*
 * Created on 11.12.2014
 * @author verkel
 */
package adversary;

import noppabot.PeekableRandom;

public class AdversaryRandom extends PeekableRandom {

	@Override
	protected int doIntRoll(int sides) {
		double val = random.nextDouble() * 100d + 1d;
		int modified = (int)Math.round(Math.sqrt(sides * val));
//		System.out.printf("transform %d -> %d\n", val, modified);
		return modified;
	}
}
