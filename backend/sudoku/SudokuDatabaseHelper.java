package backend.sudoku;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SudokuDatabaseHelper {

    private static final String DB_PATH = "database/sudoku.db";
    private static final int GRIDS_PER_DIFFICULTY = 3;
    private static final String EASY_GRID_1_SIGNATURE = "004608910670195048098302507850760423026053701703920850961037084207409630340286079";
    private static final String[][] DEFAULT_GRIDS = {
            // FACILE
            {
                    "004608910670195048098302507850760423026053701703920850961037084207409630340286079",
                    "534678912672195348198342567859761423426853791713924856961537284287419635345286179",
                    "FACILE"
            },
            {
                    "207900350169073084508504210970125038015409702420780915091057801702340690684012037",
                    "247981356169273584538564219976125438815439762423786915391657841752348693684912537",
                    "FACILE"
            },
            {
                    "907650840028017509405390710673012084509603270840179035054701408390820657086065103",
                    "937652841128417569465398712673512984519643278842179635254731498391824657786965123",
                    "FACILE"
            },

            // MOYEN
            {
                    "800420350034085007506200490180052043003801900490370021061008205300620710057030409",
                    "819427356234985167576213498186952743723861954495374821961748235348629712657130489",
                    "MOYEN"
            },
            {
                    "230980045079003105800710320097064002308100460460037019006108700730250018018003204",
                    "231986745679423185845715326197864532358192467462537819926148753734259618518673294",
                    "MOYEN"
            },
            {
                    "670518043008703600150420089065001304700830160031074002502100830480062015006809400",
                    "679518243248793651153426789865291374724835169931674582592147836487362915316859427",
                    "MOYEN"
            },

            // DIFFICILE
            {
                    "810050049003002100600490080054007006300800720080069004001900300430026017006308400",
                    "812753649943682175675491283154237896369845721287169534521974368438526917796318452",
                    "DIFFICILE"
            },
            {
                    "120037008008600400370080026005003200700240010012009004200300780030098002801700690",
                    "126437958958621473374985126495863217783246519612579384269314785537198462841752693",
                    "DIFFICILE"
            },
            {
                    "087054001200100980050028006008500600630090057005001800500280070072009008800740210",
                    "987654321246173985351928746128537694634892157795461832519286473472319568863745219",
                    "DIFFICILE"
            }
    };

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
                        CREATE TABLE IF NOT EXISTS grids (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            puzzle TEXT NOT NULL,
                            solution TEXT NOT NULL,
                            difficulty TEXT NOT NULL
                        )
                    """;
            conn.createStatement().execute(createTable);

            String createScoreTable = """
                        CREATE TABLE IF NOT EXISTS session_score (
                            id INTEGER PRIMARY KEY CHECK (id = 1),
                            score INTEGER NOT NULL
                        )
                    """;
            conn.createStatement().execute(createScoreTable);

            ensureDifficultyColumn(conn);
            ensureFixedDefaultGrids(conn);

        } catch (Exception e) {
            System.err.println("Erreur BDD Sudoku : " + e.getMessage());
        }
    }

    private static int getGridCount(Connection conn) throws Exception {
        ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM grids");
        return rs.getInt(1);
    }

    private static int getGridCountByDifficulty(Connection conn, String difficulty) throws Exception {
        String sql = "SELECT COUNT(*) FROM grids WHERE difficulty = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, difficulty);
        ResultSet rs = pstmt.executeQuery();
        return rs.getInt(1);
    }

    // Migration: ajoute la colonne difficulty si la base provient d'une ancienne
    // version.
    private static void ensureDifficultyColumn(Connection conn) throws Exception {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getColumns(null, null, "grids", "difficulty")) {
            if (!rs.next()) {
                conn.createStatement()
                        .execute("ALTER TABLE grids ADD COLUMN difficulty TEXT NOT NULL DEFAULT 'FACILE'");
            }
        }
    }

    private static void addDefaultGrids(Connection conn) throws Exception {
        String sql = "INSERT INTO grids (puzzle, solution, difficulty) VALUES (?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);

        for (String[] grid : DEFAULT_GRIDS) {
            pstmt.setString(1, grid[0]);
            pstmt.setString(2, grid[1]);
            pstmt.setString(3, grid[2]);
            pstmt.executeUpdate();
        }
    }

    // Garantit 9 grilles fixes (3 par difficulté) dans un ordre déterministe.
    private static void ensureFixedDefaultGrids(Connection conn) throws Exception {
        boolean containsCurrentDataset = containsPuzzle(conn, EASY_GRID_1_SIGNATURE);

        boolean valid = getGridCount(conn) == 9
                && getGridCountByDifficulty(conn, "FACILE") == GRIDS_PER_DIFFICULTY
                && getGridCountByDifficulty(conn, "MOYEN") == GRIDS_PER_DIFFICULTY
                && getGridCountByDifficulty(conn, "DIFFICILE") == GRIDS_PER_DIFFICULTY
                && containsCurrentDataset;

        if (valid) {
            return;
        }

        conn.setAutoCommit(false);
        try {
            conn.createStatement().execute("DELETE FROM grids");
            addDefaultGrids(conn);
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private static boolean containsPuzzle(Connection conn, String puzzle) throws Exception {
        String sql = "SELECT COUNT(*) FROM grids WHERE puzzle = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, puzzle);
        ResultSet rs = pstmt.executeQuery();
        return rs.getInt(1) > 0;
    }

    // Récupérer une grille fixe selon difficulté et index (0,1,2)
    public static int[][][] getGridByDifficultyAndIndex(String difficulty, int index) {
        if (index < 0) {
            return null;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            String sql = "SELECT puzzle, solution FROM grids WHERE difficulty = ? ORDER BY id ASC LIMIT 1 OFFSET ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, difficulty);
            pstmt.setInt(2, index);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int[][] puzzle = stringToGrid(rs.getString("puzzle"));
                int[][] solution = stringToGrid(rs.getString("solution"));
                return new int[][][] { puzzle, solution };
            }
        } catch (Exception e) {
            System.err.println("Erreur lecture grille séquentielle : " + e.getMessage());
        }
        return getBuiltInGridByDifficultyAndIndex(difficulty, index);
    }

    // Récupérer toutes les grilles d'une difficulté
    public static List<int[][]> getAllPuzzlesByDifficulty(String difficulty) {
        List<int[][]> puzzles = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            String sql = "SELECT puzzle FROM grids WHERE difficulty = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, difficulty);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                puzzles.add(stringToGrid(rs.getString("puzzle")));
            }
        } catch (Exception e) {
            System.err.println("Erreur lecture puzzles : " + e.getMessage());
        }
        if (!puzzles.isEmpty()) {
            return puzzles;
        }

        String normalizedDifficulty = normalizeDifficulty(difficulty);
        for (String[] grid : DEFAULT_GRIDS) {
            if (grid[2].equals(normalizedDifficulty)) {
                puzzles.add(stringToGrid(grid[0]));
            }
        }
        return puzzles;
    }

    private static int[][][] getBuiltInGridByDifficultyAndIndex(String difficulty, int index) {
        String normalizedDifficulty = normalizeDifficulty(difficulty);
        int current = 0;

        for (String[] grid : DEFAULT_GRIDS) {
            if (!grid[2].equals(normalizedDifficulty)) {
                continue;
            }
            if (current == index) {
                return new int[][][] { stringToGrid(grid[0]), stringToGrid(grid[1]) };
            }
            current++;
        }

        return null;
    }

    private static String normalizeDifficulty(String difficulty) {
        if (difficulty == null || difficulty.isBlank()) {
            return "FACILE";
        }
        return difficulty.toUpperCase();
    }

    // Convertir une String de 81 chiffres en grille 9x9
    public static int[][] stringToGrid(String s) {
        int[][] grid = new int[9][9];
        for (int i = 0; i < 81; i++) {
            grid[i / 9][i % 9] = Character.getNumericValue(s.charAt(i));
        }
        return grid;
    }

    // Convertir une grille 9x9 en String
    public static String gridToString(int[][] grid) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : grid) {
            for (int val : row) {
                sb.append(val);
            }
        }
        return sb.toString();
    }

    public static void resetSessionScore() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            String upsert = "INSERT INTO session_score(id, score) VALUES (1, 0) "
                    + "ON CONFLICT(id) DO UPDATE SET score = excluded.score";
            conn.createStatement().execute(upsert);
        } catch (Exception e) {
            System.err.println("Erreur reset score session : " + e.getMessage());
        }
    }

    public static int getSessionScore() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT score FROM session_score WHERE id = 1");
            if (rs.next()) {
                return rs.getInt("score");
            }
        } catch (Exception e) {
            System.err.println("Erreur lecture score session : " + e.getMessage());
        }
        return 0;
    }

    public static int addToSessionScore(int points) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            String upsert = "INSERT INTO session_score(id, score) VALUES (1, ?) "
                    + "ON CONFLICT(id) DO UPDATE SET score = score + excluded.score";
            PreparedStatement pstmt = conn.prepareStatement(upsert);
            pstmt.setInt(1, points);
            pstmt.executeUpdate();
            return getSessionScore();
        } catch (Exception e) {
            System.err.println("Erreur ajout score session : " + e.getMessage());
        }
        return getSessionScore();
    }
}