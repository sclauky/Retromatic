package backend.hangman;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class HangmanModel {

    private static final int MAX_ERRORS = 6;

    private String secretWord;
    private Set<Character> guessedLetters;
    private Set<Character> correctLetters;
    private Set<Character> wrongLetters;
    private int errorCount;
    private boolean gameOver;
    private boolean won;

    public HangmanModel() {
        reset();
    }

    public void reset() {
        // Initialiser la base de données
        DatabaseHelper.initDatabase();

        // Choisir un mot aléatoire
        List<String> words = DatabaseHelper.getAllWords();
        if (words.isEmpty()) {
            words = DatabaseHelper.getBuiltInWords();
        }

        Random random = new Random();
        if (words.isEmpty()) {
            throw new IllegalStateException("Aucun mot disponible pour le pendu.");
        } else {
            secretWord = words.get(random.nextInt(words.size()));
        }

        guessedLetters = new HashSet<>();
        correctLetters = new HashSet<>();
        wrongLetters = new HashSet<>();
        errorCount = 0;
        gameOver = false;
        won = false;
    }

    public boolean guessLetter(char letter) {
        letter = Character.toUpperCase(letter);

        // Vérifier si la lettre a déjà été devinée
        if (guessedLetters.contains(letter)) {
            return false; // Déjà devinée
        }

        guessedLetters.add(letter);

        // Vérifier si la lettre est dans le mot
        if (secretWord.indexOf(letter) >= 0) {
            correctLetters.add(letter);
            checkWin();
            return true;
        } else {
            wrongLetters.add(letter);
            errorCount++;
            if (errorCount >= MAX_ERRORS) {
                gameOver = true;
                won = false;
            }
            return false;
        }
    }

    private void checkWin() {
        for (char c : secretWord.toCharArray()) {
            if (!correctLetters.contains(c)) {
                return;
            }
        }
        gameOver = true;
        won = true;
    }

    // Obtenir le mot avec les lettres trouvées
    public String getDisplayWord() {
        StringBuilder display = new StringBuilder();
        for (char c : secretWord.toCharArray()) {
            if (correctLetters.contains(c)) {
                display.append(c).append(" ");
            } else {
                display.append("_ ");
            }
        }
        return display.toString().trim();
    }

    // Getters
    public String getSecretWord() {
        return secretWord;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getRemainingTries() {
        return MAX_ERRORS - errorCount;
    }

    public Set<Character> getWrongLetters() {
        return new HashSet<>(wrongLetters);
    }

    public Set<Character> getGuessedLetters() {
        return new HashSet<>(guessedLetters);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean hasWon() {
        return won;
    }

    public boolean isLetterGuessed(char letter) {
        return guessedLetters.contains(Character.toUpperCase(letter));
    }
}