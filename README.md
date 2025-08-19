# Dating Platform -Personality Match Android App
This Android application is a matchmaking platform based on MBTI personality types, preferences, and user interaction. It enables users to complete personality quizzes, specify personal preferences, view potential matches, chat, and receive alerts — all through a clean and interactive mobile interface.
## 💡 App Features & User Journey

This app offers a complete personality-based matchmaking experience. Below is an overview of the main features and how users interact with them through the different screens in the app:

- **🔐 User Registration & Login**  
  Secure authentication using email and password.

- **🧠 Personality Quiz (`Activity_quiz_mbti.java`)**  
  Users begin by completing an MBTI personality test that determines their psychological type.

- **📋 Lifestyle & Preferences Questionnaire (`Activity_questionnaire.java`)**  
  After the MBTI quiz, users answer additional lifestyle and personal questions (e.g., habits, hobbies, values) to refine their profile.

- **🛠️ Preferences Setup (`activity_preferences.java`)**  
  Users can set their preferences for potential matches, such as age range, interests, and other criteria.

- **📊 Personality Result (`Activity_personality_result.java`)**  
  Displays the user's MBTI type and a brief description of their personality traits, helping them understand themselves better.

- **🧩 Matchmaking Algorithm**  
  Based on MBTI compatibility and lifestyle preferences, the app suggests personalized potential matches.

- **💬 Chat System (`ChatActivity.java`, `Conversations.java`)**  
  Users can start real-time conversations with their matches using a chat interface.

- **👤 User Profiles (`ProfileActivity.java`)**  
  Each user has a profile page displaying their name, personality type, and preferences. Users can view and edit their own profile.

- **📣 Alerts & Notifications (`AlertsActivity.java`)**  
  The system generates alerts when a new match is found, a message is received, or a profile update is needed.

- **📍 Main Navigation (`HomeActivity.java`)**  
  A central hub screen where users can access quizzes, preferences, matches, chats, and their profile.

- **🎯 Match Selection (`ChooseUserForChat.java`)**  
  A screen where the user can see a list of recommended matches and choose who to chat with.

## Technologies Used
- **Android SDK**: For building the Android application.
- **Firebase**: For user authentication, real-time database, and cloud messaging.
- **Java**: Programming languages used for app development.
- **XML**: For designing the user interface layouts.
- **Gradle**: For dependency management and build configuration.
- **Retrofit**: For network operations and API calls.
- **Glide**: For image loading and caching.

## Architecture Overview
The app is structured around multiple Activities representing each logical part of the user journey. Communication between screens is handled through Intents and activity transitions. User data is stored and retrieved via appropriate storage mechanisms (assumed to be Firebase, Room, or a server API — please update if relevant).
## Major Components
| File                               | Description                                                 |
|------------------------------------| ----------------------------------------------------------- |
| `Activity_quiz_mbti.java`          | Presents MBTI questions, processes responses.               |
| `Activity_questionnaire.java`      | Handles general user preferences questionnaire.             |
| `ChooseUserForChat.java`           | Displays list of potential matches.                         |
| `ChatActivity.java`                | Chat interface with message input and RecyclerView display. |
| `Conversations.java`               | Chat list preview screen (or message history).              |
| `ProfileActivity.java`             | Allows users to manage their profile and MBTI info.         |
| `AlertsActivity.java`              | Displays custom alerts/notifications in-app.                |
| `HomeActivity.java`                | The app’s main menu and navigation hub.                     |
| `Activity_personality_result.java` | Displays the result of the MBTI quiz and personality type.  |
| `activity_preferences.java`        | Allows users to set their preferences for matches.          |


## Getting Started
To run this project, follow these steps:
## Configuration Instructions
To run the app properly, you must manually configure your local IP address in the following two places:
1. **`ApiConfig.java`**: Update the `BASE_URL` constant to your local server's IP address.

    ```bash
    app/src/main/java/com/example/meeting_project/apiClients/ApiConfig.java
    ```

   ```java
   public static final String BASE_IP = "http://YOUR_LOCAL_IP";
   ```
   This will enable the app to communicate with your backend microservices (e.g., chat, user, MBTI, etc).

2. **`network_security_config.xml`**: Ensure that the `domain` tag includes your local IP address.

    ```xml
    app/src/main/res/xml/network_security_config.xml
    ```
    Edit the file to allow cleartext HTTP traffic to your development IP address. Replace the <domain> entries accordingly:

   ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <network-security-config>
        <domain-config cleartextTrafficPermitted="true">
            <domain includeSubdomains="true">*.*.*.*</domain>
            <domain includeSubdomains="true">YOUR_LOCAL_IP</domain>
        </domain-config>
    </network-security-config>
    ```

## Tip: You can find your local IP address using the command ipconfig (Windows) or ifconfig (Linux/macOS), under IPv4 Address.

## Build & Run
1. Clone this repository:
   ```bash
   git clone https://github.com/GalAngel15/Meeting_Final_Project_Android.git
   ```
2. Open the project in Android Studio.
3. Make sure you have the necessary SDKs and dependencies installed.
4. Sync Gradle & install dependencies
5. Connect an Android device or start an emulator.
6. Run the app from Android Studio.
7. Follow the on-screen instructions to register, take the MBTI quiz, set preferences, and start matching with other users.
