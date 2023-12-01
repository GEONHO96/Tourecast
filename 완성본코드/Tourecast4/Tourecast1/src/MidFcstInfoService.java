import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;

public class MidFcstInfoService {
    private String SERVICE_KEY = "Z0CBjlusl%2FbTyFfDMByGOai8KLnLvYkIBRIdq2IfP%2B7h%2F7BFTnT77H0iZL12S40g%2FgRLT83tynbMJQ6t%2FidOQQ%3D%3D";
    private String jdbcUrl = "jdbc:mysql://localhost:3306/touristspot?characterEncoding=UTF-8&useUnicode=true";
    private String username = "root";
    private String password = "00000000";
    public void setMidta(String date) throws IOException {
        String query = "SELECT code FROM area_code;";

        String deleteQuery = "DELETE FROM midta;";

        try (
                Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)
        ) {
            deleteStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (
                Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet area_code = preparedStatement.executeQuery();
        ) {
            while (area_code.next()) {
                addMidTa(area_code.getString("code"), date);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public String addMidTa(String area_code, String date) throws IOException {
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1360000/MidFcstInfoService/getMidTa");
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + SERVICE_KEY);
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("10", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode("XML", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("regId", "UTF-8") + "=" + URLEncoder.encode(area_code, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("tmFc", "UTF-8") + "=" + URLEncoder.encode(date, "UTF-8"));

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        String insertQuery = "insert into midta( regId," +
                " taMin3 , taMax3," +
                " taMin4 , taMax4," +
                " taMin5 , taMax5," +
                " taMin6 , taMax6," +
                " taMin7 , taMax7," +
                " taMin8 , taMax8," +
                " taMin9 , taMax9," +
                " taMin10 , taMax10) values (" + "\'" + area_code + "\', ";
        String insertValue = " ";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(new InputSource(new StringReader(sb.toString())));

            Element rootElement = document.getDocumentElement();

            NodeList itemList = document.getElementsByTagName("item");
            for (int i = 0; i < itemList.getLength(); i++) {
                Element itemElement = (Element) itemList.item(i);

                for(int j = 3; j <= 10; j++ ){
                    insertValue += itemElement.getElementsByTagName("taMin" + Integer.toString(j)).item(0).getTextContent() + ",";
                    insertValue += itemElement.getElementsByTagName("taMax" + Integer.toString(j)).item(0).getTextContent() + ",";
                }
                insertValue = insertValue.substring(0, insertValue.length() - 1) + ");";

                insertQuery += insertValue;
                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
                preparedStatement.executeUpdate();
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
    public String getMidLandFcst(String area_code, String date) throws IOException {
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1360000/MidFcstInfoService/getMidLandFcst");
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + SERVICE_KEY);
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("10", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode("XML", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("regId", "UTF-8") + "=" + URLEncoder.encode(area_code, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("tmFc", "UTF-8") + "=" + URLEncoder.encode(date, "UTF-8"));

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

            String insertQuery = "UPDATE MidLandFcst SET ";

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(new InputSource(new StringReader(sb.toString())));

            Element rootElement = document.getDocumentElement();

            NodeList itemList = document.getElementsByTagName("item");
            for (int i = 0; i < itemList.getLength(); i++) {
                Element itemElement = (Element) itemList.item(i);

                for(int j = 3; j <= 7; j++) {
                    insertQuery += " rnSt" + Integer.toString(j) + "Am = " + itemElement.getElementsByTagName("rnSt" + Integer.toString(j) + "Am").item(0).getTextContent() + ",";
                    insertQuery += " rnSt" + Integer.toString(j) + "Pm = " + itemElement.getElementsByTagName("rnSt" + Integer.toString(j) + "Pm").item(0).getTextContent() + ",";

                }
                for(int j = 8; j <= 10; j++) {
                    insertQuery += " rnSt" + Integer.toString(j) + " = " + itemElement.getElementsByTagName("rnSt" + Integer.toString(j)).item(0).getTextContent() + ",";

                }
                for(int j = 3; j <= 7; j++) {
                    insertQuery += " wf" + Integer.toString(j) + "Am = \'" + itemElement.getElementsByTagName("wf" + Integer.toString(j) + "Am").item(0).getTextContent() + "\',";
                    insertQuery += " wf" + Integer.toString(j) + "Pm = \'" + itemElement.getElementsByTagName("wf" + Integer.toString(j) + "Pm").item(0).getTextContent() + "\',";

                }
                for(int j = 8; j <= 10; j++) {
                    insertQuery += " wf" + Integer.toString(j) + " = \'" + itemElement.getElementsByTagName("wf" + Integer.toString(j)).item(0).getTextContent() + "\',";

                }

                insertQuery += " update_time = \'" + date + "\' WHERE area_code = \'"  + area_code + "\';";
                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
                preparedStatement.executeUpdate();
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
