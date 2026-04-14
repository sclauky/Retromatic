package backend.sudoku;

public class SudokuModel {

    private int[][] solution;  // La grille complète résolue
    private int[][] puzzle;    // La grille avec les cases vides
    private int[][] userGrid;  // Ce que le joueur a rempli
    private String difficulty; // stocke la difficulté

    public SudokuModel() {
    }

    // Charger une grille depuis la base
    public void loadGrid(int[][] puzzle, int[][] solution) {
        this.solution = solution;
        this.puzzle = puzzle;
        this.userGrid = new int[9][9];

        // Copier le puzzle dans la grille du joueur
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                userGrid[i][j] = puzzle[i][j];
            }
        }
    }

    // Charger avec difficulté
    public void loadGrid(int[][] puzzle, int[][] solution, String difficulty) {
        loadGrid(puzzle, solution);
        this.difficulty = difficulty;
    }

    // Le joueur entre un chiffre
    public boolean setNumber(int row, int col, int number) {
        if (puzzle[row][col] != 0) return false;
        userGrid[row][col] = number;
        return true;
    }

    // Vérifier si le chiffre entré est correct
    public boolean isCorrect(int row, int col) {
        return userGrid[row][col] == solution[row][col];
    }

    // Vérifier si la grille est complète et correcte
    public boolean isComplete() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (userGrid[i][j] != solution[i][j]) return false;
            }
        }
        return true;
    }

    // Savoir si une case est pré-remplie 
    public boolean isPreFilled(int row, int col) {
        return puzzle[row][col] != 0;
    }

    public int[][] getUserGrid() { return userGrid; }
    public int[][] getPuzzle()   { return puzzle; }
    public int[][] getSolution() { return solution; }
    public String getDifficulty() { return difficulty; } // NOUVEAU
}