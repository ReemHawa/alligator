package model;

public class gameHistory {

    private final String date;
    private final String playerA;
    private final String playerB;
    private final String result;
    private final String duration;
    private final int score;
    private final String level;

    public gameHistory(
            String date,
            String playerA,
            String playerB,
            String result,
            String duration,
            int score,
            String level) {

        this.date = date;
        this.playerA = playerA;
        this.playerB = playerB;
        this.result = result;
        this.duration = duration;
        this.score = score;
        this.level = level;
    }

    public String getDate() {
        return date;
    }

    public String getPlayerA() {
        return playerA;
    }

    public String getPlayerB() {
        return playerB;
    }

    public String getResult() {
        return result;
    }

    public String getDuration() {
        return duration;
    }

    public int getScore() {
        return score;
    }

    public String getLevel() {
        return level;
    }
}
