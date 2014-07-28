/*
 * Created on 10.7.2014
 * @author verkel
 */
package noppabot;

import java.util.*;

import noppabot.StringUtils.StringConverter;
import noppabot.spawns.*;
import noppabot.spawns.Spawner.SpawnInfo;
import noppabot.spawns.dice.*;

import com.google.common.collect.*;

public class PeekedRoll {

	public final int sides;
	public final DiceRoll value;
	public ClassToInstanceMap<Hint> hints = MutableClassToInstanceMap.create();

	private final List<Hint> allHints = Arrays.<Hint> asList(new LowValueHint(), new MedValueHint(),
		new HighValueHint(), new ExactValueHint(), new PrimeHint(), new SevensHint(),
		new ExtremalHint());

	public PeekedRoll(int sides, DiceRoll value) {
		this.sides = sides;
		this.value = value;
	}
	
	public String summarizeAll() {
		return StringUtils.join(hints.values(), ", ", new StringConverter<Hint>() {

			@Override
			public String toString(Hint value) {
				return value.summarize();
			}
			
		});
	}
	
	/**
	 * Spawn a hint, and return it. If no hints can be spawned, spawn nothing and return null.
	 */
	public Hint spawnHint() {
		List<Hint> spawnableHints = spawnableHints();
		if (spawnableHints.isEmpty()) return null;
		Spawner<Hint> spawner = new Spawner<Hint>(spawnableHints::stream);
		Hint hint = spawner.spawn();
		hint.removeObsoleteHints();
		hints.put(hint.getClass(), hint);
		return hint;
	}
	
	public boolean canSpawnHint() {
		return !spawnableHints().isEmpty();
	}
	
	private List<Hint> spawnableHints() {
		List<Hint> spawnableHints = new ArrayList<Hint>();
		for (Hint hint : allHints) {
			if (hint.canSpawn()) spawnableHints.add(hint);
		}
		return spawnableHints;
	}
	
	public boolean hasHint(Hint h) {
		return hasHint(h.getClass());
	}
	
	public boolean hasHint(Class<? extends Hint> c) {
		return hints.containsKey(c);
	}
	
	public abstract class Hint implements SpawnInfo<Hint>, ISpawnable {

		private String name;

		public Hint(String name) {
			this.name = name;
		}

		@Override
		public String toStringColored() {
			return name;
		}

		@Override
		public double spawnChance() {
			return 0;
		}
		
		@Override
		public Hint create() {
			return this;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public String nameWithDetails() {
			return name;
		}

		public abstract String tell();
		
		public abstract String summarize();
		
		public boolean canSpawn() {
			// Hint can spawn if it hasn't spawned already, it applies and a better
			// hint of same type has not spawned already
			return !hasHint(getClass()) && applies() && !isObsolete();
		}

		protected abstract boolean applies();
		
		protected boolean isObsolete() {
			return hasHint(ExactValueHint.class);
		}
		
		public void removeObsoleteHints() {
		}
	}

	abstract class ValueHint extends Hint {

		public ValueHint() {
			super("Value hint");
		}
		
		@Override
		public String summarize() {
			return "is " + hint();
		}
		
		abstract protected String hint();
		
		@Override
		protected boolean applies() {
			return true;
		}
	}

	class LowValueHint extends ValueHint {

		@Override
		public String tell() {
			return "Your future is cloudy... the roll will be " + hint() + ".";
		}

		@Override
		protected String hint() {
			return valueStr(sides / 2);
		}

		@Override
		protected boolean isObsolete() {
			return hasHint(MedValueHint.class) || hasHint(HighValueHint.class)
				|| hasHint(ExactValueHint.class);
		}
		
		@Override
		public double spawnChance() {
			return 0.5f;
		}
	}

	class MedValueHint extends ValueHint {

		@Override
		public String tell() {
			return "I see your roll. It will be " + hint() + "!";
		}

		@Override
		protected String hint() {
			return valueStr(sides / 5);
		}

		@Override
		protected boolean isObsolete() {
			return hasHint(HighValueHint.class) || hasHint(ExactValueHint.class);
		}
		
		@Override
		public double spawnChance() {
			return 1f;
		}
		
		@Override
		public void removeObsoleteHints() {
			hints.remove(LowValueHint.class);
		}
	}

	class HighValueHint extends ValueHint {

		@Override
		public String tell() {
			return "I see your roll clearly! It will be " + hint() + "!";
		}

		@Override
		protected String hint() {
			return valueStr(sides / 10);
		}

		@Override
		public double spawnChance() {
			return 0.5f;
		}
		
		@Override
		public void removeObsoleteHints() {
			hints.remove(LowValueHint.class);
			hints.remove(MedValueHint.class);
		}
	}

	class ExactValueHint extends ValueHint {

		@Override
		public String tell() {
			return "The NopPantheon have revealed your roll! It will be " + hint() + "!";
		}

		@Override
		protected String hint() {
			return Color.emphasize(value.intValue());
		}

		@Override
		public boolean canSpawn() {
			return hasHint(HighValueHint.class);
		}
		
		@Override
		public double spawnChance() {
			return 0.5f;
		}
		
		@Override
		public void removeObsoleteHints() {
			hints.clear();
		}
	}
	
	class PrimeHint extends Hint {

		public PrimeHint() {
			super("Prime hint");
		}

		@Override
		public String tell() {
			return "Your roll will be a prime!";
		}

		@Override
		public String summarize() {
			return "is a prime";
		}

		@Override
		protected boolean applies() {
			return PrimalDie.primes.contains(value.intValue());
		}
		
		@Override
		public double spawnChance() {
			return 0.5f;
		}

	}
	
	class SevensHint extends Hint {

		public SevensHint() {
			super("Sevens hint");
		}

		@Override
		public String tell() {
			return "Your roll will contain sevens!";
		}

		@Override
		public String summarize() {
			return "contains sevens";
		}

		@Override
		protected boolean applies() {
			return LuckyDie.containsSevens(value);
		}
		
		@Override
		public double spawnChance() {
			return 0.5f;
		}
	}

	class ExtremalHint extends Hint {

		public ExtremalHint() {
			super("Extremal hint");
		}

		@Override
		public String tell() {
			return "Your roll will be extremal!";
		}

		@Override
		public String summarize() {
			return "is extremal";
		}

		@Override
		protected boolean applies() {
			return value.test(i -> i <= 10 || i >= 90);
		}
		
		@Override
		public double spawnChance() {
			return 0.5f;
		}
	}
	
	private String valueStr(int incr) {
		if (value.intValue() < 1) return String.valueOf(value);
		
		int lowBound = 1, highBound = incr;
		while (highBound < value.intValue()) {
			lowBound += incr;
			highBound += incr;
		}
		return "between " + Color.emphasize(lowBound) + " … " + Color.emphasize(highBound);
	}
}
