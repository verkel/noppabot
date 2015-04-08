/*
 * Created on 11.12.2014
 * @author verkel
 */
package adversary;

import java.util.function.Supplier;

import noppabot.PeekableRandom;

public class WeightedRandom extends PeekableRandom {

	private Supplier<Bias> bs;

	public static Bias NO_BIAS = new Bias(1.0, true);
	
	public static class Bias {
		/**
		 * @param bias Number from ]0, 1]. Even distribution is produced by 1,
		 *           approaching 0 produces increasingly uneven distribution
		 * @param highRolls True if the distribution is skewed towards high rolls,
		 *           false if towards low.
		 */
		public Bias(double bias, boolean highRolls) {
			this.bias = bias;
			this.highRolls = highRolls;
		}
		public final double bias;
		public final boolean highRolls;
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(bias);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + (highRolls ? 1231 : 1237);
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Bias other = (Bias) obj;
			if (Double.doubleToLongBits(bias) != Double
					.doubleToLongBits(other.bias))
				return false;
			if (highRolls != other.highRolls)
				return false;
			return true;
		}
	}
	
	public WeightedRandom(double bias, boolean highRolls) {
		this(new Supplier<Bias>() {

			private Bias b = new Bias(bias, highRolls);

			@Override
			public Bias get() {
				return b;
			}

		});
	}

	/**
	 * Produces an uneven roll distribution based on given bias
	 */
	public WeightedRandom(Supplier<Bias> cfg) {
		this.bs = cfg;
	}
	
	@Override
	protected int doIntRoll(int sides) {
		double val = random.nextDouble() * sides;
		Bias b = bs.get();
		// modifiedRoll(roll, diff) = sides^(1-diff) * roll^diff
		int modified = (int)(Math.pow(sides, 1d - b.bias) * Math.pow(val, b.bias));
		if (b.highRolls) return modified + 1;
		else return sides - modified;
	}
}
