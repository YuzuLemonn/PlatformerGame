package utilz;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Leaderboard {
    private static final int MAX_ENTRIES = 10;
    private static final Path FILE_PATH = Paths.get("leaderboard.txt");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    public static class Entry {
        private final String name;
        private final long timeMillis;
        private final long completedAtMillis;

        public Entry(String name, long timeMillis, long completedAtMillis) {
            this.name = name;
            this.timeMillis = timeMillis;
            this.completedAtMillis = completedAtMillis;
        }

        public String getName() {
            return name;
        }

        public long getTimeMillis() {
            return timeMillis;
        }

        public String getFormattedTime() {
            return formatTime(timeMillis);
        }

        public String getFormattedCompletedAt() {
            return DATE_FORMAT.format(Instant.ofEpochMilli(completedAtMillis));
        }
    }

    public List<Entry> getEntries() {
        List<Entry> entries = readEntries();
        entries.sort(Comparator.comparingLong(Entry::getTimeMillis));
        if (entries.size() > MAX_ENTRIES)
            return new ArrayList<>(entries.subList(0, MAX_ENTRIES));
        return entries;
    }

    public void addEntry(String rawName, long timeMillis) {
        if (timeMillis <= 0)
            return;

        List<Entry> entries = readEntries();
        entries.add(new Entry(sanitizeName(rawName), timeMillis, System.currentTimeMillis()));
        entries.sort(Comparator.comparingLong(Entry::getTimeMillis));

        if (entries.size() > MAX_ENTRIES)
            entries = new ArrayList<>(entries.subList(0, MAX_ENTRIES));

        writeEntries(entries);
    }

    public static String sanitizeName(String rawName) {
        if (rawName == null)
            return "Player";

        String trimmed = rawName.trim();
        if (trimmed.isEmpty())
            return "Player";

        StringBuilder clean = new StringBuilder();
        for (int i = 0; i < trimmed.length() && clean.length() < 16; i++) {
            char c = trimmed.charAt(i);
            if (Character.isLetterOrDigit(c) || c == ' ' || c == '_' || c == '-')
                clean.append(c);
        }

        String result = clean.toString().trim();
        return result.isEmpty() ? "Player" : result;
    }

    public static String formatTime(long timeMillis) {
        long totalSeconds = timeMillis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long tenths = (timeMillis % 1000) / 100;
        return String.format("%02d:%02d.%d", minutes, seconds, tenths);
    }

    private List<Entry> readEntries() {
        List<Entry> entries = new ArrayList<>();
        if (!Files.exists(FILE_PATH))
            return entries;

        try {
            for (String line : Files.readAllLines(FILE_PATH, StandardCharsets.UTF_8)) {
                Entry entry = parseEntry(line);
                if (entry != null)
                    entries.add(entry);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return entries;
    }

    private Entry parseEntry(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length != 3)
            return null;

        try {
            return new Entry(sanitizeName(parts[0]), Long.parseLong(parts[1]), Long.parseLong(parts[2]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void writeEntries(List<Entry> entries) {
        List<String> lines = new ArrayList<>();
        for (Entry entry : entries)
            lines.add(entry.getName() + "," + entry.getTimeMillis() + "," + entry.completedAtMillis);

        try {
            Files.write(FILE_PATH, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
