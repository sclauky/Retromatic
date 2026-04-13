package backend.fusion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FusionModel {

    public static final int SIZE = 4;
    private final int[][] grid = new int[SIZE][SIZE];
    private int score = 0;
    private final Random random = new Random();

    private boolean[][] merged = new boolean[SIZE][SIZE];

    public FusionModel() {
        reset();
    }

    public void reset() {
        score = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = 0;
                merged[r][c] = false;
            }
        }
        addRandomTile();
        addRandomTile();
    }

    public int getScore() {
        return score;
    }

    public int[][] getGrid() {
        int[][] copy = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(grid[r], 0, copy[r], 0, SIZE);
        }
        return copy;
    }

    private void addRandomTile() {
        List<int[]> empty = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] == 0) empty.add(new int[]{r, c});
            }
        }
        if (empty.isEmpty()) return;
        int[] pos = empty.get(random.nextInt(empty.size()));
        grid[pos[0]][pos[1]] = random.nextDouble() < 0.9 ? 2 : 4;
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public boolean move(Direction dir) {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                merged[r][c] = false;

        boolean moved = false;
        switch (dir) {
            case LEFT -> moved = moveLeft();
            case RIGHT -> moved = moveRight();
            case UP -> moved = moveUp();
            case DOWN -> moved = moveDown();
        }
        if (moved) addRandomTile();
        return moved;
    }

    private boolean moveLeft() {
        boolean moved = false;
        for (int r = 0; r < SIZE; r++) {
            int[] line = grid[r];
            int[] mergedLine = mergeLine(compactLine(line), r, true);
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] != mergedLine[c]) moved = true;
                grid[r][c] = mergedLine[c];
            }
        }
        return moved;
    }

    private boolean moveRight() {
        boolean moved = false;
        for (int r = 0; r < SIZE; r++) {
            int[] line = reverse(grid[r]);
            int[] mergedLine = mergeLine(compactLine(line), r, false);
            mergedLine = reverse(mergedLine);
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] != mergedLine[c]) moved = true;
                grid[r][c] = mergedLine[c];
            }
        }
        return moved;
    }

    private boolean moveUp() {
        boolean moved = false;
        for (int c = 0; c < SIZE; c++) {
            int[] col = new int[SIZE];
            for (int r = 0; r < SIZE; r++) col[r] = grid[r][c];
            int[] mergedCol = mergeLine(compactLine(col), c, true);
            for (int r = 0; r < SIZE; r++) {
                if (grid[r][c] != mergedCol[r]) moved = true;
                grid[r][c] = mergedCol[r];
            }
        }
        return moved;
    }

    private boolean moveDown() {
        boolean moved = false;
        for (int c = 0; c < SIZE; c++) {
            int[] col = new int[SIZE];
            for (int r = 0; r < SIZE; r++) col[r] = grid[r][c];
            col = reverse(col);
            int[] mergedCol = mergeLine(compactLine(col), c, false);
            mergedCol = reverse(mergedCol);
            for (int r = 0; r < SIZE; r++) {
                if (grid[r][c] != mergedCol[r]) moved = true;
                grid[r][c] = mergedCol[r];
            }
        }
        return moved;
    }

    private int[] compactLine(int[] line) {
        int[] result = new int[SIZE];
        int idx = 0;
        for (int value : line) if (value != 0) result[idx++] = value;
        return result;
    }

    private int[] mergeLine(int[] line, int fixed, boolean normalOrder) {
        int[] result = new int[SIZE];
        int idx = 0;
        for (int i = 0; i < SIZE; i++) {
            if (line[i] == 0) continue;
            if (i < SIZE - 1 && line[i] == line[i + 1]) {
                int v = line[i] * 2;
                result[idx] = v;
                score += v;

                if (normalOrder) merged[fixed][idx] = true;
                else merged[idx][fixed] = true;

                idx++;
                i++;
            } else {
                result[idx++] = line[i];
            }
        }
        return result;
    }

    private int[] reverse(int[] line) {
        int[] res = new int[SIZE];
        for (int i = 0; i < SIZE; i++) res[i] = line[SIZE - 1 - i];
        return res;
    }

    public boolean hasWon() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (grid[r][c] == 2048) return true;
        return false;
    }

    public boolean hasMoves() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (grid[r][c] == 0) return true;

        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++) {
                int v = grid[r][c];
                if (r + 1 < SIZE && grid[r + 1][c] == v) return true;
                if (c + 1 < SIZE && grid[r][c + 1] == v) return true;
            }
        return false;
    }

    public boolean wasMerged(int r, int c) {
        return merged[r][c];
    }
}