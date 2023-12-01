
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;

public class SpotDAO {
    private String url = "jdbc:mysql://localhost:3306/touristspot";
    private String username = "root";
    private String password = "00000000";
    private String SERVICE_KEY = "Z0CBjlusl%2FbTyFfDMByGOai8KLnLvYkIBRIdq2IfP%2B7h%2F7BFTnT77H0iZL12S40g%2FgRLT83tynbMJQ6t%2FidOQQ%3D%3D";

    public String currentSixOrEighteen(){
        Calendar calendar = Calendar.getInstance();
        Date currentDate = (Date) calendar.getTime();

        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.get(Calendar.HOUR_OF_DAY) >= 6 && calendar.get(Calendar.HOUR_OF_DAY) <= 17) {
            calendar.set(Calendar.HOUR_OF_DAY, 6);
        } else if (calendar.get(Calendar.HOUR_OF_DAY) >= 18 && calendar.get(Calendar.HOUR_OF_DAY) <= 23) {
            calendar.set(Calendar.HOUR_OF_DAY, 18);
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 18);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        String formattedDate = dateFormat.format(calendar.getTime());

        return formattedDate;
    }
    public String getMidta(String code, int after){
        String query = "SELECT * FROM midta WHERE regId = \'" + code + "\';";
        String result = "";
        try (
                Connection connection = DriverManager.getConnection(url, username, password);
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            while (resultSet.next()) {
                result += ("최저 기온: " + resultSet.getString("taMin" + after) + ", 최고 기온: " + resultSet.getString("taMax" + after));
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
    public String getAreaCode(String spot_name) {
        String regex = "\\((.*?)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(spot_name);
        String area_name;
        if (matcher.find()) {
            if(matcher.group(1).equals(("(서천희리산해송자연휴양림")) || matcher.group(1).equals("(서천서천특화시장")){
                area_name = "서천";
            }
            area_name = matcher.group(1);
            if(area_name.equals("고셩")){
                area_name = "고성";
            } else if(area_name.equals("체천")){
                area_name = "제천";
            } else if(area_name.equals("환순")){
                area_name = "화순";
            } else if(area_name.equals("군위")){
                area_name = "대구";
            }
        } else {
            switch (spot_name){
                case "돈대산":
                    area_name = "진도";
                    break;
                default:
                    area_name = "추자";

            }
        }
        String code;

        String query = "SELECT code FROM area_code WHERE name LIKE \'%" + area_name + "%\';";
        try (
                Connection connection = DriverManager.getConnection(url, username, password);
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            while (resultSet.next()) {
                code = resultSet.getString("code");
                return code;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String findClosestFutureDate(String inputMonth, String inputDay) {
        LocalDate currentDate = LocalDate.now();

        LocalDate inputDate = LocalDate.of(currentDate.getYear(), Integer.parseInt(inputMonth), Integer.parseInt(inputDay));

        if (inputDate.isBefore(currentDate)) {
            inputDate = inputDate.plusYears(1);
        }

        return inputDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    public int compareDates(String baseDateStr, String compareDateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate baseDate = LocalDate.parse(baseDateStr, formatter);
        LocalDate compareDate = LocalDate.parse(compareDateStr, formatter);

        long daysDifference = ChronoUnit.DAYS.between(baseDate, compareDate);

        if (daysDifference >= 3 && daysDifference <= 10) {
            return (int) daysDifference;
        } else if (daysDifference < 3) {
            return -1;
        } else {
            return 0;
        }
    }

    public List<String> getSpotData(String theme, String area, String indoor, String month, String date, String weather){
        List<String> result = new ArrayList<>();

        String query = "SELECT ";
        Boolean compare = false;
        int inout = 0;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        int isCorrectedDate = compareDates(LocalDate.now().format(formatter),findClosestFutureDate(month, date));

        if(isCorrectedDate == -1 || isCorrectedDate == 0) {
            result.add("날짜 오류");
            return result;
        }

        if(!area.equals("상관 없음") && weather.equals("상관 없음")) {
            query = "SELECT spot_name, theme_name, indoor FROM " + area + " ";

            if(!theme.equals("상관 없음")) {
                query += " WHERE theme_name = \'" + theme + "\'";
                compare = true;
            }

            if(!indoor.equals("상관 없음")) {
                if(indoor.equals("실내"))
                    inout = 1;

                if(compare == true){
                    query += " AND indoor = " + inout;
                } else {
                    query += " WHERE indoor = " + inout;
                    compare = true;
                }
            }

            query += ";";

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                Statement weatherStmt = connection.createStatement();

                String findweatherData = "select ";

                if(isCorrectedDate <= 7) {
                    findweatherData += "wf" + Integer.toString(isCorrectedDate) + "Am, " + "wf" + Integer.toString(isCorrectedDate) + "Pm " + " from midlandfcst where " ;
                } else {
                    findweatherData += "wf" + Integer.toString(isCorrectedDate) + " from midlandfcst where ";
                }
                ResultSet weatherResult = weatherStmt.executeQuery(findweatherData + " area LIKE \'%" + area + "%\';");
                //System.out.println(findweatherData + " area LIKE \'%" + area + "%\';");
                weatherResult.next();
                String weatherData;
                if(isCorrectedDate <= 7){
                    weatherData = " 오전: " + weatherResult.getString(1) + ", 오후: " + weatherResult.getString(2);
                } else {
                    weatherData = " 전체: " + weatherResult.getString(1);
                }

                weatherResult.close();
                try (Statement statement = connection.createStatement();
                     ResultSet resultSet = statement.executeQuery(query)) {
                    // 결과 처리
                    while (resultSet.next()) {
                        String name = resultSet.getString("spot_name");
                        String theme_name = resultSet.getString("theme_name");
                        String isindoor;
                        if(resultSet.getBoolean("indoor")){
                            isindoor = "실내";
                        } else {
                            isindoor = "실외";
                        }
                        String area_code = getAreaCode(name);
                        String result_data = name + "#" + theme_name + "#" + isindoor + "#" + weatherData + "#" + getMidta(getAreaCode(name),isCorrectedDate);
                        result.add(result_data);
                    }
                } catch (Exception e){

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else if (area.equals("상관 없음") && !weather.equals("상관 없음")){
            String findWeather = "select area from midlandfcst where ";
            String findweatherData = "select ";

            if(isCorrectedDate <= 7) {
                String tail = "(wf" + Integer.toString(isCorrectedDate) + "Am LIKE \'%" + weather.charAt(0) + "%\' OR " + "wf" + Integer.toString(isCorrectedDate) + "Pm LIKE \'%" + weather.charAt(0) + "%\')";
                findWeather += tail;
                findweatherData += "wf" + Integer.toString(isCorrectedDate) + "Am, " + "wf" + Integer.toString(isCorrectedDate) + "Pm " + " from midlandfcst where " + tail;
            } else {
                String tail = "(wf" + Integer.toString(isCorrectedDate) + " LIKE \'%" + weather.charAt(0) + "%\')";
                findWeather += tail;
                findweatherData += "wf" + Integer.toString(isCorrectedDate) + " from midlandfcst where " + tail;
            }

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                String where = "";

                if(!theme.equals("상관 없음")) {
                    where += " WHERE theme_name = \'" + theme + "\'";
                    compare = true;
                }

                if(!indoor.equals("상관 없음")) {
                    if(indoor.equals("실내"))
                        inout = 1;

                    if(compare == true){
                        where += " AND indoor = " + inout;
                    } else {
                        where += " WHERE indoor = " + inout;
                        compare = true;
                    }
                }

                try (Statement statement = connection.createStatement();
                     ResultSet area_name = statement.executeQuery(findWeather)) {

                    while (area_name.next()) {
                        String n = area_name.getString("area");
                        List<String> areas = new ArrayList<>();
                        areas = Arrays.asList(n.split(","));

                        for (String a : areas) {
                            try(Statement resultStmt = connection.createStatement();
                                Statement weatherStmt = connection.createStatement()){

                                System.out.println(findweatherData +" AND AREA LIKE \'%" + a + "%\';");
                                ResultSet weatherResult = weatherStmt.executeQuery(findweatherData +" AND AREA LIKE \'%" + a + "%\';");
                                //System.out.println(findweatherData +"AND AREA LIKE \'%" + a + "%\';");
                                weatherResult.next();
                                String weatherData;
                                if(isCorrectedDate <= 7){
                                    weatherData = " 오전: " + weatherResult.getString(1) + ", 오후: " + weatherResult.getString(2);
                                } else {
                                    weatherData = " 전체: " + weatherResult.getString(1);
                                }

                                query = "SELECT spot_name, theme_name, indoor from " + a + where;
                                ResultSet resultSet = resultStmt.executeQuery(query);

                                while (resultSet.next()){
                                    String name = resultSet.getString("spot_name");
                                    String theme_name = resultSet.getString("theme_name");
                                    String isindoor;
                                    if(resultSet.getBoolean("indoor")){
                                        isindoor = "실내";
                                    } else {
                                        isindoor = "실외";
                                    }
                                    String result_data = name + "#" + theme_name + "#" + isindoor + "#" + weatherData + "#" + getMidta(getAreaCode(name),isCorrectedDate);
                                    result.add(result_data);
                                }
                                resultSet.close();
                                weatherResult.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String[] areas = {"서울", "경기도", "인천", "강원도영서", "강원도영동", "대전", "세종", "충청남도", "충청북도", "광주", "전라남도", "전라북도", "대구", "경상북도", "부산", "울산", "경상남도", "제주도"};

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                for (String a: areas){
                    query = "SELECT spot_name, theme_name, indoor FROM " + a + " ";

                    if(!theme.equals("상관 없음")) {
                        query += " WHERE theme_name = \'" + theme + "\'";
                        compare = true;
                    }

                    if(!indoor.equals("상관 없음")) {
                        if(indoor.equals("실내"))
                            inout = 1;

                        if(compare == true){
                            query += " AND indoor = " + inout;
                        } else {
                            query += " WHERE indoor = " + inout;
                            compare = true;
                        }
                    }
                    query += ";";
                    Statement weatherStmt = connection.createStatement();

                    String findweatherData = "select ";

                    if(isCorrectedDate <= 7) {
                        findweatherData += "wf" + Integer.toString(isCorrectedDate) + "Am, " + "wf" + Integer.toString(isCorrectedDate) + "Pm " + " from midlandfcst where " ;
                    } else {
                        findweatherData += "wf" + Integer.toString(isCorrectedDate) + " from midlandfcst where ";
                    }
                    ResultSet weatherResult = weatherStmt.executeQuery(findweatherData + " area LIKE \'%" + a + "%\';");
                    weatherResult.next();
                    String weatherData;

                    if(isCorrectedDate <= 7){
                        weatherData = " 오전: " + weatherResult.getString(1) + ", 오후: " + weatherResult.getString(2);
                    } else {
                        weatherData = " 전체: " + weatherResult.getString(1);
                    }
                    weatherResult.close();
                    try (Statement resultStmt = connection.createStatement()){
                        ResultSet resultSet = resultStmt.executeQuery(query);

                        while (resultSet.next()){
                            String name =  resultSet.getString("spot_name");
                            String theme_name = resultSet.getString("theme_name");
                            String isindoor;
                            if(resultSet.getBoolean("indoor")){
                                isindoor = "실내";
                            } else {
                                isindoor = "실외";
                            }
                            String result_data = name + "#" + theme_name + "#" + isindoor + "#" + weatherData + "#" + getMidta(getAreaCode(name),isCorrectedDate);
                            result.add(result_data);
                        }
                        resultSet.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

}
