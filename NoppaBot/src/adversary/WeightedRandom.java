/*
 * Created on 11.12.2014
 * @author verkel
 */
package adversary;

import noppabot.PeekableRandom;

public class WeightedRandom extends PeekableRandom {

	private double bias;
	private boolean highRolls;

	/**
	 * Produces an uneven roll distribution based on given bias
	 * @param bias Number from ]0, 1]. Even distribution is produced by 1, approaching 0 produces increasingly uneven distribution
	 * @param highRolls True if the distribution is skewed towards high rolls, false if towards low.
	 */
	public WeightedRandom(double bias, boolean highRolls) {
		this.bias = bias;
		this.highRolls = highRolls;
	}
	
	@Override
	protected int doIntRoll(int sides) {
		double val = random.nextDouble() * 100d;
		// modifiedRoll(roll, diff) = sides^(1-diff) * roll^diff
		int modified = (int)(Math.pow(sides, 1d - bias) * Math.pow(val, bias));
		if (highRolls) return modified + 1;
		else return 100 - modified;
	}
}
