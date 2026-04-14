package backend.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryModel {

    public static final String[] ICONS = {
    "First Contact", "Irene", "Josef", "The Computer Room",
    "The GMO", "The Office", "The Swim", "Vincent"
    };

    public static class Card {
        public final String icon;
        public boolean flipped = false;
        public boolean matched = false;

        public Card(String icon) {
            this.icon = icon;
        }
    }

    private List<Card> cards;
    private Card firstFlipped = null;
    private Card secondFlipped = null;
    private int moves = 0;
    private int matchedPairs = 0;

    public MemoryModel() {
        reset();
    }

    public void reset() {
        cards = new ArrayList<>();
        for (String icon : ICONS) {
            cards.add(new Card(icon));
            cards.add(new Card(icon));
        }
        Collections.shuffle(cards);
        firstFlipped = null;
        secondFlipped = null;
        moves = 0;
        matchedPairs = 0;
    }

    public List<Card> getCards() {
        return cards;
    }

    public int getMoves() {
        return moves;
    }

    public int getMatchedPairs() {
        return matchedPairs;
    }

    public boolean isGameOver() {
        return matchedPairs == ICONS.length;
    }

    public Card getFirstFlipped() {
        return firstFlipped;
    }

    public Card getSecondFlipped() {
        return secondFlipped;
    }

    public boolean canFlip(Card card) {
        return !card.flipped && !card.matched && secondFlipped == null;
    }

    public void flip(Card card) {
        card.flipped = true;
        if (firstFlipped == null) {
            firstFlipped = card;
        } else {
            secondFlipped = card;
            moves++;
        }
    }

    public boolean checkMatch() {
        if (firstFlipped != null && secondFlipped != null) {
            if (firstFlipped.icon.equals(secondFlipped.icon)) {
                firstFlipped.matched = true;
                secondFlipped.matched = true;
                matchedPairs++;
                firstFlipped = null;
                secondFlipped = null;
                return true;
            }
        }
        return false;
    }

    public void unflipTwo() {
        if (firstFlipped != null) firstFlipped.flipped = false;
        if (secondFlipped != null) secondFlipped.flipped = false;
        firstFlipped = null;
        secondFlipped = null;
    }
}