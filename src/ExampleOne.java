import java.sql.*;

public class ExampleOne {

    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:user.db");
            System.out.println("Connected");
            PreparedStatement stmt = (PreparedStatement) connection.createStatement();
            String loginOne = "login1";
            String passOne = "pass1";

            String query = "SELECT * FROM users WHERE login=? AND pass=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, loginOne);
            preparedStatement.setString(2, passOne);

            ResultSet rs = preparedStatement.executeQuery(query);
            while (rs.next()) {
                System.out.println("id: " + rs.getInt("id") + " " +
                        "login: " + rs.getString("login") + " " +
                        "pass: " + rs.getString("password") + " " +
                        "nick: " + rs.getString("nick"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
