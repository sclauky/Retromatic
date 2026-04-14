package backend.fusion;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FusionLeaderboardService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String supabaseUrl;
    private final String supabaseAnonKey;

    public FusionLeaderboardService() {
        Map<String, String> dotenv = loadDotenv();
        this.supabaseUrl = readConfig("SUPABASE_URL", dotenv);
        this.supabaseAnonKey = readConfig("SUPABASE_ANON_KEY", dotenv);
    }

    public record ScoreEntry(String guestName, int score) {}
    public record SubmissionResult(String guestName, int score) {}

    public boolean isConfigured() {
        return supabaseUrl != null && !supabaseUrl.isBlank()
                && supabaseAnonKey != null && !supabaseAnonKey.isBlank();
    }

    public SubmissionResult submitNextGuestScore(int score) throws IOException, InterruptedException {
        ensureConfigured();

        int nextGuestNumber = findNextGuestNumber();
        String guestName = "Guest " + nextGuestNumber;
        String payload = "{\"guest_name\":\"" + escapeJson(guestName) + "\",\"score\":" + score + "}";

        HttpRequest request = baseRequest("/rest/v1/scores")
                .header("Content-Type", "application/json")
                .header("Prefer", "return=minimal")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201 && response.statusCode() != 200 && response.statusCode() != 204) {
            throw new IOException("Insert score failed: HTTP " + response.statusCode() + " - " + response.body());
        }

        return new SubmissionResult(guestName, score);
    }

    public List<ScoreEntry> getTopScores(int limit) throws IOException, InterruptedException {
        ensureConfigured();
        String path = "/rest/v1/scores?select=guest_name,score&order=score.desc,created_at.asc&limit=" + limit;
        HttpRequest request = baseRequest(path).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Fetch leaderboard failed: HTTP " + response.statusCode() + " - " + response.body());
        }

        return parseScoreEntries(response.body());
    }

    private int findNextGuestNumber() throws IOException, InterruptedException {
        String path = "/rest/v1/scores?select=guest_name&order=id.desc&limit=1";
        HttpRequest request = baseRequest(path).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Fetch guests failed: HTTP " + response.statusCode() + " - " + response.body());
        }

        Pattern pattern = Pattern.compile("\\\"guest_name\\\"\\s*:\\s*\\\"Guest\\s+(\\d+)\\\"");
        Matcher matcher = pattern.matcher(response.body());
        if (!matcher.find()) return 1;
        return Integer.parseInt(matcher.group(1)) + 1;
    }

    private HttpRequest.Builder baseRequest(String path) {
        String base = supabaseUrl.endsWith("/") ? supabaseUrl.substring(0, supabaseUrl.length() - 1) : supabaseUrl;
        return HttpRequest.newBuilder()
                .uri(URI.create(base + path))
                .timeout(Duration.ofSeconds(10))
                .header("apikey", supabaseAnonKey)
                .header("Authorization", "Bearer " + supabaseAnonKey)
                .header("Accept", "application/json");
    }

    private List<ScoreEntry> parseScoreEntries(String json) {
        List<ScoreEntry> entries = new ArrayList<>();
        Pattern objectPattern = Pattern.compile("\\{[^{}]*}");
        Matcher objectMatcher = objectPattern.matcher(json);

        while (objectMatcher.find()) {
            String object = objectMatcher.group();
            String guestName = extractStringField(object, "guest_name");
            Integer score = extractIntField(object, "score");
            if (guestName != null && score != null) {
                entries.add(new ScoreEntry(guestName, score));
            }
        }

        return entries;
    }

    private String extractStringField(String jsonObject, String fieldName) {
        Pattern fieldPattern = Pattern.compile("\\\"" + Pattern.quote(fieldName) + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"");
        Matcher matcher = fieldPattern.matcher(jsonObject);
        if (!matcher.find()) return null;
        return unescapeJson(matcher.group(1));
    }

    private Integer extractIntField(String jsonObject, String fieldName) {
        Pattern fieldPattern = Pattern.compile("\\\"" + Pattern.quote(fieldName) + "\\\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = fieldPattern.matcher(jsonObject);
        if (!matcher.find()) return null;
        return Integer.parseInt(matcher.group(1));
    }

    private String readConfig(String key, Map<String, String> dotenv) {
        String fromEnv = System.getenv(key);
        if (fromEnv != null && !fromEnv.isBlank() && !fromEnv.startsWith("https://YOUR-") && !fromEnv.startsWith("YOUR_")) {
            return fromEnv;
        }

        String fromDotenv = dotenv.get(key);
        if (fromDotenv != null && !fromDotenv.isBlank()) {
            return fromDotenv;
        }

        return null;
    }

    private Map<String, String> loadDotenv() {
        Map<String, String> values = new HashMap<>();
        Path dotenvPath = Path.of(System.getProperty("user.dir"), ".env");
        if (!Files.exists(dotenvPath)) {
            return values;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(dotenvPath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

                int separator = trimmed.indexOf('=');
                if (separator <= 0) continue;

                String key = trimmed.substring(0, separator).trim();
                String rawValue = trimmed.substring(separator + 1).trim();
                values.put(key, stripQuotes(rawValue));
            }
        } catch (IOException ignored) {
            // If .env cannot be read, fallback remains System.getenv only.
        }

        return values;
    }

    private String stripQuotes(String value) {
        if (value.length() >= 2) {
            boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
            boolean singleQuoted = value.startsWith("'") && value.endsWith("'");
            if (doubleQuoted || singleQuoted) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    private String escapeJson(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private String unescapeJson(String input) {
        return input
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private void ensureConfigured() {
        if (!isConfigured()) {
            throw new IllegalStateException("SUPABASE_URL and SUPABASE_ANON_KEY must be configured.");
        }
    }
}