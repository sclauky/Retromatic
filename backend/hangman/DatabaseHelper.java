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

        if (getWordCount(conn) == 0) {
            addDefaultWords(conn);
        }

    } catch (Exception e) {
        System.err.println("Erreur BDD : " + e.getMessage());
    }
}
    
    // Compter le nombre de mots dans la base
    private static int getWordCount(Connection conn) throws Exception {
        String sql = "SELECT COUNT(*) FROM words";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        return rs.getInt(1);
    }
    
    // Ajouter des mots par défaut (si base de donnée vidz)
    private static void addDefaultWords(Connection conn) throws Exception {
        String[] defaultWords = {
            "FUSEE", "PLANETE", "ETOILE", "SATELLITE",
            "ASTRONAUTE", "GALAXIE", "COMETE", "MARS",
            "JUPITER", "SATURNE", "NEPTUNE", "URANUS",
            "TELESCOPE", "COSMOS", "ORBITE", "METEOR",
            "APOLLO", "GRAVITE", "NEBULEUSE", "LUNE",
            "SOLEIL", "MERCURE", "VENUS", "TERRE",
            "CONSTELLATION", "ASTRO", "ESPACE", "VAISSEAU"
        };
        
        String sql = "INSERT OR IGNORE INTO words (word) VALUES (?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        
        for (String word : defaultWords) {
            pstmt.setString(1, word);
            pstmt.executeUpdate();
        }
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