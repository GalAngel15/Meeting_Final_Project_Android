package com.example.meeting_project.apiClients;

public class ApiConfig {
    //public static final String BASE_IP = "http://192.168.1.89";
    //yahav home wifi
    public static final String BASE_IP = "http://192.168.68.110";
    //emulator address
    //public static final String BASE_IP = "http://10.0.2.2";
    //diana home wifi
    //public static final String BASE_IP = "http://192.168.68.109";

    //public static final String BASE_IP = "http://192.168.37.49";


    // שירותים שונים לפי פורט/נתיב
    public static final String CHATS_BASE_URL     = BASE_IP + ":8080/";
    public static final String USERS_BASE_URL     = BASE_IP + ":8081/";
    public static final String QUESTIONS_BASE_URL = BASE_IP + ":8082/";
    public static final String MBTI_SERVICE_URL   = BASE_IP + ":8083/";
    public static final String MATCH_BASE_URL     = BASE_IP + ":8084/";
    public static final String MBTI_TEST_URL      = BASE_IP + ":3000/";

    private ApiConfig() {
        // מניעת יצירת אובייקט
    }
}
