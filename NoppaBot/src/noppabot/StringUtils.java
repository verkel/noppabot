/*
 * Created on 14.1.2014
 * @author verkel
 */
package noppabot;

import java.util.Iterator;


public class StringUtils {
	public static interface StringConverter<T> {
		public String toString(T value);
	}
	
	public static StringConverter<Object> defaultStringConverter = new StringConverter<Object>() {
		@Override
		public String toString(Object value) {
			return value.toString();
		};
	};
	
	public static StringConverter<IColorStrConvertable> colorStringConverter = new StringConverter<IColorStrConvertable>() {
		@Override
		public String toString(IColorStrConvertable value) {
			return value.toStringColored();
		};
	};

	
	public static <T> String join(Iterable<T> s, String delimiter) {
		return join(s, delimiter, defaultStringConverter);
	}
	
	public static <T> String join(Iterable<T> iterable, String delimiter, StringConverter<? super T> converter) {
	    Iterator<T> iter = iterable.iterator();
	    if (!iter.hasNext()) return "";
	    StringBuilder buffer = new StringBuilder(converter.toString(iter.next()));
	    while (iter.hasNext()) buffer.append(delimiter).append(converter.toString(iter.next()));
	    return buffer.toString();
	}
	
	public static String joinColored(Iterable<? extends IColorStrConvertable> iterable, String delimiter) {
		return join(iterable, delimiter, colorStringConverter);
	}
	
}
