package com.example.learn_express;

public class Constants {
    // Updated IP address for real phone connection
    public static final String BASE_URL = "http://192.168.1.110/learn_express_app/e-learnin-api/";
    
    public static final String LOGIN_URL = BASE_URL + "login.php";
    public static final String SIGNUP_URL = BASE_URL + "signup.php";
    
    // Add other endpoints as you develop them
    public static final String COURSES_URL = BASE_URL + "get_courses.php";
}
