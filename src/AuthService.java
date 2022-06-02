public interface AuthService {
    void start();

    String getNickByLoginPass(String login, String pass);

    int getUserIDByLoginPass(String login, String pass);

    boolean nicknameIsUsed(String nickname);

    String ChangeNick(int userID, String newNickname);

    void addUser(String login, String pass, String nick);

    void deleteUser(String nick);

    void stop();
}