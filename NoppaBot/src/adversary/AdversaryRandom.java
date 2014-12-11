/*
 * Created on 11.12.2014
 * @author verkel
 */
package adversary;

import noppabot.PeekableRandom;

public class AdversaryRandom extends PeekableRandom {

	@Override
	protected int doIntRoll(int sides) {
		int val = super.doIntRoll(sides);
		int modified = (int)Math.round(Math.sqrt(100 * val));
//		System.out.printf("transform %d -> %d\n", val, modified);
		return modified;
	}
}
