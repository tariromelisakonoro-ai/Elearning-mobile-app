package com.example.learn_express;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static DataManager instance;
    private List<Question> questions = new ArrayList<>();
    private List<Announcement> announcements = new ArrayList<>();

    private DataManager() {}

    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public List<Question> getQuestions() { return questions; }
    public void addQuestion(Question q) { questions.add(0, q); }

    public List<Announcement> getAnnouncements() { return announcements; }
    public void addAnnouncement(Announcement a) { announcements.add(0, a); }

    public static class Question {
        public String title, detail, subject;
        public Question(String title, String detail, String subject) {
            this.title = title;
            this.detail = detail;
            this.subject = subject;
        }
    }

    public static class Announcement {
        public String title, content;
        public Announcement(String title, String content) {
            this.title = title;
            this.content = content;
        }
    }
}