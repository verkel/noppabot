/*
 * Created on 27.7.2014
 * @author verkel
 */
package noppabot;

@FunctionalInterface
public interface PropertyChangeListener<T> {
	public void propertyChanged(T oldValue, T newValue, T defaultValue);
}