import java.sql.*;

public class BaseAuthService implements AuthService {
    private final static String connectionUrl = "jdbc:sqlite:users.db";
    private static Connection connection;
    private static Statement stmt;
    @Override
    public void start() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(connectionUrl);
            stmt = connection.createStatement();
            System.out.println("Сервис аутентификации запущен");
            FillDummyUsersIfEmpty();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Сервис аутентификации не запущен");
        }
    }
    @Override
    public void stop() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Сервис аутентификации остановлен");
    }
    public BaseAuthService() {
    }

    @Override
    public boolean nicknameIsUsed(String nickname) {
        try {
            return stmt.executeQuery(String.format("SELECT * FROM users WHERE nick='%s'", nickname)).next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        try {
            String query = String.format("SELECT * FROM users WHERE login='%s' AND pass = '%s'", login, pass);
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                return rs.getString("nick");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public int getUserIDByLoginPass(String login, String pass) {
        try {
            String query = String.format("SELECT * FROM users WHERE login='%s' AND pass = '%s'", login, pass);
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    @Override
    public String ChangeNick(int userID, String newNickname) {
        try {
            String query = String.format("UPDATE users SET nick = '%s' WHERE id = %d", newNickname, userID);
            stmt.execute(query);
            query = String.format("SELECT * FROM users WHERE id='%d'", userID);
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                return rs.getString("nick");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addUser(String login, String pass, String nick) {
        try {
            if (!stmt.executeQuery(String.format("SELECT * FROM users WHERE login='%s' OR nick='%s'", login, nick)).next()) {
                String query = String.format("INSERT INTO users (login, pass, nick) VALUES ('%s', '%s', '%s')", login, pass, nick);
                stmt.execute(query);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteUser(String nick) {
        try {
            String query = String.format("DELETE FROM users WHERE nick='%s'", nick);
            stmt.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void FillDummyUsersIfEmpty() {
        try {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (login TEXT NOT NULL, pass TEXT NOT NULL, nick TEXT NOT NULL)");
            if (!stmt.executeQuery("SELECT * FROM users").next()) {
                for (int i = 1; i <= 3; i++)
                    stmt.addBatch(String.format("INSERT INTO users (login, pass, nick) VALUES (\"login%d\", \"pass%d\", \"nick%d\")", i, i, i));
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
