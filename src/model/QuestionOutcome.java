package model;

public class QuestionOutcome {

    public int pointsDelta;
    public int livesDelta;

    public boolean revealOneMine;
    public boolean reveal3x3Random;

    public String message;

    public QuestionOutcome(int pointsDelta, int livesDelta,
                           boolean revealOneMine,
                           boolean reveal3x3Random,
                           String message) {
        this.pointsDelta = pointsDelta;
        this.livesDelta = livesDelta;
        this.revealOneMine = revealOneMine;
        this.reveal3x3Random = reveal3x3Random;
        this.message = message;
    }
    
    
    
    
}
