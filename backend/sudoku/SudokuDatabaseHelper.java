package backend.sudoku;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SudokuDatabaseHelper {

    private static final String DB_PATH = "database/sudoku.db";

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
                    solution TEXT NOT NULL
                )
            """;
            conn.createStatement().execute(createTable);

            if (getGridCount(conn) == 0) {
                addDefaultGrids(conn);
            }

        } catch (Exception e) {
            System.err.println("Erreur BDD Sudoku : " + e.getMessage());
        }
    }

    private static int getGridCount(Connection conn) throws Exception {
        ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM grids");
        return rs.getInt(1);
    }

    // Chaque grille est stockée comme une String de 81 chiffres (0 = case vide)
    private static void addDefaultGrids(Connection conn) throws Exception {
        String[][] grids = {
            {
                "530070000600195000098000060800060003400803001700020006060000280000419005000080079",
                "534678912672195348198342567859761423426853791713924856961537284287419635345286179"
            },
            {
                "010020300004005060070000008006900070000800904500074020900000050040600100007030040",
                "819427356234985167576213498186952743723861954495374821961748235348629712657130489"
            },
            {
                "200080300060070084030500209000105408000000000402706000301007040720040060004010003",
                "247981356169273584538564219976125438815439762423786915391657841752348693684912537"
            },
            {
                "000000907000420180000705026100904000050000040000507009920108000034059000507000000",
                "231986745679423185845715326197864532358192467462537819926148753734259618518673294"
            },
            {
                "030050040008010500460000012070502080000603000040109030250000098003080600080060020",
                "937652841128417569465398712673512984519643278842179635254731498391824657786965123"
            }
        };

        String sql = "INSERT INTO grids (puzzle, solution) VALUES (?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);

        for (String[] grid : grids) {
            pstmt.setString(1, grid[0]);
            pstmt.setString(2, grid[1]);
            pstmt.executeUpdate();
        }
    }

    // Récupérer toutes les grilles
    public static List<int[][]> getAllPuzzles() {
        List<int[][]> puzzles = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT puzzle FROM grids");
            while (rs.next()) {
                puzzles.add(stringToGrid(rs.getString("puzzle")));
            }
        } catch (Exception e) {
            System.err.println("Erreur lecture puzzles : " + e.getMessage());
        }
        return puzzles;
    }

    // Récupérer une grille aléatoire (puzzle + solution)
    public static int[][][] getRandomGrid() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT puzzle, solution FROM grids ORDER BY RANDOM() LIMIT 1"
            );
            if (rs.next()) {
                int[][] puzzle   = stringToGrid(rs.getString("puzzle"));
                int[][] solution = stringToGrid(rs.getString("solution"));
                return new int[][][]{puzzle, solution};
            }
        } catch (Exception e) {
            System.err.println("Erreur lecture grille aléatoire : " + e.getMessage());
        }
        return null;
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
}