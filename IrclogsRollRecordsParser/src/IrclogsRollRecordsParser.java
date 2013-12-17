import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.*;

import noppabot.RollRecords;

import org.joda.time.LocalTime;

/*
 * Created on May 19, 2012
 * @author verkel
 */

public class IrclogsRollRecordsParser {

	private static final Path dir = Paths.get("H:\\documents\\irclogs");
	private static final String mask = "#orp IRCNet*.log";
	
	public static void main(String[] args) throws IOException {
		new IrclogsRollRecordsParser().run();
	}
	
	public void run() throws IOException {
		final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**\\" + mask);
		Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (matcher.matches(file)) readLogFile(file);
				return FileVisitResult.CONTINUE;
			}
		});
		System.out.println("Files parsed!");
		System.out.println("results:");
		rollRecords.sortUsers();
//		System.out.println(rollRecords.users);
		OutputStreamWriter osw = new OutputStreamWriter(System.out);
		rollRecords.save(osw);
	}
	
	private RollRecords rollRecords = new RollRecords();
	
	private Pattern linePattern = Pattern.compile("(\\d{2}:\\d{2}(?::\\d{2})?) <(.+)> (.+)");
	private Pattern rollPattern = Pattern.compile("([^:]+):? rolls (\\d{1,3})(?:.*)?");
	
	private LocalTime rollPeriodStart = LocalTime.parse("00:00");
	private LocalTime rollPeriodEnd = LocalTime.parse("00:10");
	
	private void readLogFile(Path file) throws IOException {
		Map<String, Integer> rolls = new HashMap<String, Integer>();
		
		try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.ISO_8859_1)) {
			String line;
			boolean winnerFound = true;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("--- Day changed")) {
					winnerFound = false;
					continue;
				}
				
				Matcher lineMatcher = linePattern.matcher(line);
				if (lineMatcher.matches()) {
					String timeStr = lineMatcher.group(1);
					String nick = lineMatcher.group(2);
					String msg = lineMatcher.group(3);
					LocalTime time = LocalTime.parse(timeStr);
					
//					System.out.printf("%s %s %s\n", time, nick, msg);
					
					// Decide a winner for today
					if (!winnerFound && time.isAfter(rollPeriodEnd) && !rolls.isEmpty()) {
						List<String> highestRollers = getHighestRollers(rolls);
						if (highestRollers.size() == 1) {
							String winnerNick = highestRollers.get(0);
							rollRecords.incrementWins(winnerNick);
							winnerFound = true;
						}
					}
					
					// Parse a roll and add the result to the list if roll is qualified for contest
					if ((nick.equalsIgnoreCase("noppabotti") || nick.equalsIgnoreCase("sexysandra^") || nick.equalsIgnoreCase("proxyidra^"))) {
						Matcher rollMatcher = rollPattern.matcher(msg);
						if (rollMatcher.matches()) {
							String rollerNick = rollMatcher.group(1);
							String rollStr = rollMatcher.group(2);
							int roll = Integer.parseInt(rollStr);
							
							if (time.isAfter(rollPeriodStart) && time.isBefore(rollPeriodEnd)) {
								rolls.put(rollerNick, roll);
							}
							else if (!winnerFound) {
								rolls.put(rollerNick, roll);
							}
						}
					}
				}
			}
		}
		catch (IOException e) {
			System.err.printf("Exception when reading %s: %s\n", file, e);
		}
	}
	
	public List<String> getHighestRollers(Map<String, Integer> rolls) {
		int max = Integer.MIN_VALUE;
		List<String> highestRollers = new ArrayList<String>();
		for (String nick : rolls.keySet()) {
			int roll = rolls.get(nick);
			if (roll > max) {
				max = roll;
				highestRollers.clear();
				highestRollers.add(nick);
			}
			else if (roll == max) {
				highestRollers.add(nick);
			}
		}
		
		return highestRollers;
	}
}
