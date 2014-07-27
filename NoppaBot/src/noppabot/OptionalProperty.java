/*
 * Created on 27.7.2014
 * @author verkel
 */
package noppabot;

import java.util.Optional;
import java.util.function.Consumer;


public class OptionalProperty<T> extends Property<Optional<T>> {

	public static <T> OptionalProperty<T> create() {
		return new OptionalProperty<T>();
	}
	
	public OptionalProperty() {
		super(Optional.empty());
	}

	public boolean isPresent() {
		return get().isPresent();
	}
	
	public void ifPresent(Consumer<T> consumer) {
		get().ifPresent(consumer);
	}
	
	public T orElse(T other) {
		return get().orElse(other);
	}
	
	public T getValue() {
		return get().get();
	}
	
	public void setValue(T value) {
		set(Optional.of(value));
	}
}
