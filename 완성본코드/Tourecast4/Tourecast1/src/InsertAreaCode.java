import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
public class InsertAreaCode {

    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mysql://localhost:3306/touristspot?characterEncoding=UTF-8&useUnicode=true";
        String username = "root";
        String password = "00000000";
        String csvFilePath = "C:\\2023-2학기 강의정리,과제\\고급객체\\중기예보_중기기온예보구역코드utf-8.csv";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFilePath), "UTF-8"))) {

                String line;
                //line = br.readLine();
                Boolean b;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(","); // CSV 파일의 컬럼 구분자에 따라 수정


                    // 데이터베이스에 데이터 삽입
                    String insertQuery = "INSERT INTO area_code (name, code) VALUES (?, ?)";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                        for (int i = 0; i < data.length; i++) {
                            preparedStatement.setString(i + 1, data[i]);
                        }


                        preparedStatement.executeUpdate();
                    }
                }
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
