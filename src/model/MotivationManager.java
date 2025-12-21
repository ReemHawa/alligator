package model;

import java.util.Random;

public class MotivationManager {
    private final int[] goodStreak = new int[2];
    private final int[] badStreak  = new int[2];
    private final Random rnd = new Random();

    private static final String[] GOOD_MSG = {
        "Nice move! ✅",
        "Great choice! 🎯",
        "Smart move! 😄",
        "Keep going! 🔥"
    };

    private static final String[] BAD_MSG = {
        "So close! Try again 💪",
        "You can do better! 🙂",
        "Don’t give up! 🔁",
        "Next move will be better ✨"
    };

    
    public String onGoodMove(int playerIndex) {
        goodStreak[playerIndex]++;
        badStreak[playerIndex] = 0;

        
        if (goodStreak[playerIndex] % 2 == 0) {
            return GOOD_MSG[rnd.nextInt(GOOD_MSG.length)];
        }
        return null;
    }

    
    public String onBadMove(int playerIndex) {
        badStreak[playerIndex]++;
        goodStreak[playerIndex] = 0;


        if (badStreak[playerIndex] >= 2) {
            return BAD_MSG[rnd.nextInt(BAD_MSG.length)];
        }
        return null;
    }
}
