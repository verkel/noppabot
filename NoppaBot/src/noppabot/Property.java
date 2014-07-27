/*
 * Created on 27.7.2014
 * @author verkel
 */
package noppabot;

import java.util.*;

import com.google.common.base.Objects;

public class Property<T> {

	private T defaultValue;
	private T value;
	private List<PropertyChangeListener<T>> listeners = Collections.emptyList();

	public static <T> Property<T> of(T defaultValue) {
		return new Property<T>(defaultValue);
	}
	
	public Property(T defaultValue) {
		this.defaultValue = defaultValue;
	}

	public T get() {
		return value;
	}

	public void set(T value) {
		T oldValue = this.value;
		this.value = value;
		if (!Objects.equal(oldValue, value)) {
			for (int i = 0; i < listeners.size(); i++) {
				listeners.get(i).propertyChanged(oldValue, value, defaultValue);
			}
		}
	}
	
	public T getDefault() {
		return defaultValue;
	}

	public void addListener(SimplePropertyChangeListener<T> listener) {
		ensureListCreated();
		listeners.add(listener);
	}
	
	public void addListener(PropertyChangeListener<T> listener) {
		ensureListCreated();
		listeners.add(listener);
	}

	public void removeListener(PropertyChangeListener<T> listener) {
		listeners.remove(listener);
	}
	
	public boolean isDefault() {
		return Objects.equal(value, defaultValue);
	}
	
	public boolean isChanged() {
		return !Objects.equal(value, defaultValue);
	}
	
	public void reset() {
		set(defaultValue);
	}
	
	private void ensureListCreated() {
		if (listeners == Collections.EMPTY_LIST) listeners = new ArrayList<>();
	}
}
