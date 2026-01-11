package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Question {

    private int questionID;
    private String questionText;
    private String difficultyLevel;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    private String correctLetter; // A/B/C/D

    public Question(int questionID, String questionText, String difficultyLevel,
                    String optionA, String optionB, String optionC, String optionD,
                    String correctLetter) {

        this.questionID = questionID;
        this.questionText = questionText;
        this.difficultyLevel = difficultyLevel;

        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;

        setCorrectLetter(correctLetter);
    }

    // ===== GETTERS =====
    public int getQuestionID() { return questionID; }
    public String getQuestionText() { return questionText; }
    public String getDifficultyLevel() { return difficultyLevel; }

    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }

    public String getCorrectLetter() { return correctLetter; }

    // ===== SETTERS =====
    public void setQuestionID(int id) { this.questionID = id; }
    public void setQuestionText(String t) { this.questionText = t; }
    public void setDifficultyLevel(String s) { this.difficultyLevel = s; }

    public void setOptionA(String s) { this.optionA = s; }
    public void setOptionB(String s) { this.optionB = s; }
    public void setOptionC(String s) { this.optionC = s; }
    public void setOptionD(String s) { this.optionD = s; }

    public void setCorrectLetter(String s) {
        this.correctLetter = (s == null) ? "" : s.trim().toUpperCase();
    }

    // ===== Gameplay helpers =====

    public String getCorrectAnswerText() {
        switch (correctLetter) {
            case "A": return optionA;
            case "B": return optionB;
            case "C": return optionC;
            case "D": return optionD;
            default:  return "";
        }
    }

    public List<String> getAllAnswers() {
        List<String> answers = new ArrayList<>();
        answers.add(optionA);
        answers.add(optionB);
        answers.add(optionC);
        answers.add(optionD);
        return answers;
    }

    public List<String> getAllAnswersShuffled() {
        List<String> answers = getAllAnswers();
        Collections.shuffle(answers);
        return answers;
    }

    public boolean isCorrect(String chosen) {
        if (chosen == null) return false;
        String correct = getCorrectAnswerText();
        if (correct == null) correct = "";
        return chosen.trim().equals(correct.trim());
    }
}
