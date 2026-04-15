package backend.hangman;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    private static final String DB_PATH = "database/hangman.db";
    private static final String[] DEFAULT_WORDS = {
            "FUSEE", "PLANETE", "ETOILE", "SATELLITE",
            "ASTRONAUTE", "GALAXIE", "COMETE", "MARS",
            "JUPITER", "SATURNE", "NEPTUNE", "URANUS",
            "TELESCOPE", "COSMOS", "ORBITE", "METEOR",
            "APOLLO", "GRAVITE", "NEBULEUSE", "LUNE",
            "SOLEIL", "MERCURE", "VENUS", "TERRE",
            "CONSTELLATION", "ASTRO", "ESPACE", "VAISSEAU"
    };

    // Créer la base de données et la table si elle n'existe pas
    public static void initDatabase() {
        new java.io.File("database").mkdirs();

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver SQLite introuvable : " + e.getMessage());
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            String createTable = """
                        CREATE TABLE IF NOT EXISTS words (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            word TEXT NOT NULL UNIQUE
                        )
                    """;
            Statement stmt = conn.createStatement();
            stmt.execute(createTable);

            // Complète la base avec les mots intégrés sans dupliquer les existants.
            addDefaultWords(conn);

        } catch (Exception e) {
            System.err.println("Erreur BDD : " + e.getMessage());
        }
    }

    // Ajouter des mots par défaut (si base de donnée vidz)
    private static void addDefaultWords(Connection conn) throws Exception {
        String sql = "INSERT OR IGNORE INTO words (word) VALUES (?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);

        for (String word : DEFAULT_WORDS) {
            pstmt.setString(1, word);
            pstmt.executeUpdate();
        }
    }

    // Liste intégrée utilisée si la BDD est indisponible
    public static List<String> getBuiltInWords() {
        return new ArrayList<>(List.of(DEFAULT_WORDS));
    }

    // Récupérer tous les mots
    public static List<String> getAllWords() {
        List<String> words = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            String sql = "SELECT word FROM words";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                words.add(rs.getString("word"));
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des mots : " + e.getMessage());
        }

        return words;
    }

}