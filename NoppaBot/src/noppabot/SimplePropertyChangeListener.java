/*
 * Created on 27.7.2014
 * @author verkel
 */
package noppabot;

@FunctionalInterface
public interface SimplePropertyChangeListener<T> extends PropertyChangeListener<T> {
	public void propertyChanged(T newValue);
	
	@Override
	public default void propertyChanged(T oldValue, T newValue, T defaultValue) {
		propertyChanged(newValue);
	}
}