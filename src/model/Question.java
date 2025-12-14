package model;

public class Question {

    private int questionID;
    private String questionText;
    private String correctAnswer;
    private String wrongAnswer1;
    private String wrongAnswer2;
    private String wrongAnswer3;
    private String difficultyLevel;
    private String gameLevel;

    public Question(int questionID, String questionText,
                    String correctAnswer, String wrongAnswer1, String wrongAnswer2, String wrongAnswer3,
                    String difficultyLevel, String gameLevel) {

        this.questionID = questionID;
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
        this.wrongAnswer1 = wrongAnswer1;
        this.wrongAnswer2 = wrongAnswer2;
        this.wrongAnswer3 = wrongAnswer3;
        this.difficultyLevel = difficultyLevel;
        this.gameLevel = gameLevel;
    }

    public int getQuestionID() { return questionID; }
    public String getQuestionText() { return questionText; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getWrongAnswer1() { return wrongAnswer1; }
    public String getWrongAnswer2() { return wrongAnswer2; }
    public String getWrongAnswer3() { return wrongAnswer3; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public String getGameLevel() { return gameLevel; }

    public void setQuestionID(int id) { this.questionID = id; }
    public void setQuestionText(String t) { this.questionText = t; }
    public void setCorrectAnswer(String s) { this.correctAnswer = s; }
    public void setWrongAnswer1(String s) { this.wrongAnswer1 = s; }
    public void setWrongAnswer2(String s) { this.wrongAnswer2 = s; }
    public void setWrongAnswer3(String s) { this.wrongAnswer3 = s; }
    public void setDifficultyLevel(String s) { this.difficultyLevel = s; }
    public void setGameLevel(String s) { this.gameLevel = s; }
}
