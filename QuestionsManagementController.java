package Controller;

import Model.Question;
import View.HomeScreen;
import View.QuestionsManagementScreen;
import View.QuestionTableModel;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

public class QuestionsManagementController {

    private QuestionsManagementScreen screen;
    private List<Question> questions;
    private HomeScreen homeScreen;

    public QuestionsManagementController(HomeScreen homeScreen) {
        this.homeScreen = homeScreen;

        // כרגע: שאלות דמו לבדיקה (בהמשך נחבר לאקסל)
        questions = createDemoQuestions();

        screen = new QuestionsManagementScreen(questions);
        initListeners();

        screen.setVisible(true);
        this.homeScreen.setVisible(false); // מסתירים את המסך הראשי
    }

    // מאזינים לכפתור BACK וללחיצות על הטבלה (Delete/Edit)
    private void initListeners() {

        // כפתור חזרה
        screen.getBackButton().addActionListener(e -> {
            screen.dispose();          // סוגר את מסך השאלות
            homeScreen.setVisible(true); // מחזיר את המסך הראשי
        });

        // לחיצה על הטבלה
        screen.getQuestionsTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = screen.getQuestionsTable().rowAtPoint(e.getPoint());
                int col = screen.getQuestionsTable().columnAtPoint(e.getPoint());

                QuestionTableModel model = screen.getTableModel();

                if (row < 0) return; // לא לחצו על שורה

                if (col == 0) { // Delete
                    Question q = model.getQuestionAt(row);
                    int confirm = JOptionPane.showConfirmDialog(
                            screen,
                            "Delete question #" + q.getQuestionID() + "?",
                            "Confirm delete",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (confirm == JOptionPane.YES_OPTION) {
                        model.removeQuestionAt(row);
                        // בהמשך נוסיף פה שמירה לאקסל
                    }

                } else if (col == 1) { // Edit
                    JOptionPane.showMessageDialog(
                            screen,
                            "Edit for question #" + model.getQuestionAt(row).getQuestionID() +
                                    " not implemented yet 🙂"
                    );
                }
            }
        });
    }

    // רשימת שאלות לדוגמה
    private List<Question> createDemoQuestions() {
        return Arrays.asList(
                new Question(1,
                        "What is the main purpose of the SRS document?",
                        "To clearly define the software requirements.",
                        "To design the system architecture.",
                        "To write the test cases for validation.",
                        "To document the project after deployment.",
                        "Medium", "Medium"),
                new Question(2,
                        "Which of the following best describes the Agile development model?",
                        "An iterative and incremental approach focused on adapting to change.",
                        "A linear model where each phase must be completed before the next.",
                        "A model used only for hardware development.",
                        "A model in which requirements never change.",
                        "Medium", "Easy")
        );
    }
}
