import java.sql.*;
import java.util.*;

public class Randomspot {
    private String query = "SELECT * FROM touristspot.all_spot WHERE course_id = ";

    private String jdbcUrl = "jdbc:mysql://localhost:3306/touristspot?characterEncoding=UTF-8&useUnicode=true";
    private String username = "root";
    private String password = "00000000";
    public List<String> randomspot(int n) {
        List<String> result = new ArrayList<>();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query + Integer.toString(n) + ";")) {
                while (resultSet.next()) {
                    String spot_name = resultSet.getString("spot_name");
                    String course_order = Integer.toString(resultSet.getInt("course_order"));
                    String travel_time = Integer.toString(resultSet.getInt("travel_time"));
                    String indoor;
                    if(resultSet.getBoolean("indoor")) {
                        indoor = "실내";
                    } else {
                        indoor = "실외";
                    }
                    String theme_name = resultSet.getString("theme_name");
                    result.add(spot_name +"#" + course_order + "#" + travel_time + "#" + indoor + "#" + theme_name);
                    //System.out.println(spot_name +"#" + course_order + "#" + travel_time + "#" + indoor + "#" + theme_name);
                }
            }
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}