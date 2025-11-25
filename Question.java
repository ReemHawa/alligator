package Model;

public class Question {

    private int questionID;
    private String questionText;
    private String correctAnswer;
    private String wrongAnswer1;
    private String wrongAnswer2;
    private String wrongAnswer3;
    private String difficultyLevel; // Easy / Medium / Hard / Expert
    private String gameLevel;       // Easy / Medium / Hard (רמת המשחק)

    public Question(int questionID, String questionText,
                    String correctAnswer,
                    String wrongAnswer1, String wrongAnswer2, String wrongAnswer3,
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

    public int getQuestionID() {
        return questionID;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getWrongAnswer1() {
        return wrongAnswer1;
    }

    public String getWrongAnswer2() {
        return wrongAnswer2;
    }

    public String getWrongAnswer3() {
        return wrongAnswer3;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public String getGameLevel() {
        return gameLevel;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

	public void setQuestionID(int questionID) {
		this.questionID = questionID;
	}

	public void setCorrectAnswer(String correctAnswer) {
		this.correctAnswer = correctAnswer;
	}

	public void setWrongAnswer1(String wrongAnswer1) {
		this.wrongAnswer1 = wrongAnswer1;
	}

	public void setWrongAnswer2(String wrongAnswer2) {
		this.wrongAnswer2 = wrongAnswer2;
	}

	public void setWrongAnswer3(String wrongAnswer3) {
		this.wrongAnswer3 = wrongAnswer3;
	}

	public void setDifficultyLevel(String difficultyLevel) {
		this.difficultyLevel = difficultyLevel;
	}
//set
	public void setGameLevel(String gameLevel) {
		this.gameLevel = gameLevel;
	}

}
