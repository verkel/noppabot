/*
 * Created on 31.3.2015
 * @author verkel
 */
package noppabot.spawns.events;

import java.io.*;
import java.util.*;

public class Fortunes {

	private Random rnd = new Random();
	private List<String> fortunes = new ArrayList<>();

	public Fortunes() {
		parse();
	}
	
	private void parse() {
		InputStream s = getClass().getResourceAsStream("fortunes");
		try (BufferedReader r = new BufferedReader(new InputStreamReader(s))) {
			String line; StringBuilder buf = new StringBuilder();
			while ((line = r.readLine()) != null) {
				if (line.equals("%")) {
					fortunes.add(buf.toString());
					buf.delete(0, buf.length());
				}
				else {
					buf.append(line);
				}
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String random() {
		int i = rnd.nextInt(fortunes.size());
		return fortunes.get(i);
	}
}
