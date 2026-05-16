package com.example.learn_express;

public class Material {
    private String id;
    private String type;
    private String title;
    private String description;
    private String courseTitle;
    private String instructorName;
    private String dueDate;
    private String gradingCriteria;
    private String fileUrl;

    public Material(String id, String type, String title, String description, String courseTitle, String instructorName, String dueDate, String gradingCriteria, String fileUrl) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
        this.courseTitle = courseTitle;
        this.instructorName = instructorName;
        this.dueDate = dueDate;
        this.gradingCriteria = gradingCriteria;
        this.fileUrl = fileUrl;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCourseTitle() { return courseTitle; }
    public String getInstructorName() { return instructorName; }
    public String getDueDate() { return dueDate; }
    public String getGradingCriteria() { return gradingCriteria; }
    public String getFileUrl() { return fileUrl; }
}
