package rhit.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class VersionManager {
  public static boolean isLatestVersion(String path) {
    File versionFile = new File(path, "version.json");
    if (!versionFile.exists()) {
      return false;
    }
    try (InputStreamReader reader =
        new InputStreamReader(new FileInputStream(versionFile), StandardCharsets.UTF_8)) {
      JSONObject versionData = (JSONObject) new JSONParser().parse(reader);
      int version =
          Integer.parseInt(((String) versionData.get("version")).replaceAll("[^0-9]", ""));
      String updateUrl = (String) versionData.get("update_url");
      int remoteVersion = getRemoteVersion(updateUrl);
      return remoteVersion == -1 || remoteVersion <= version;

    } catch (Exception e) {
      System.err.println(e.getMessage());
      return true;
    }
  }

  private static int getRemoteVersion(String updateUrl) {
    HttpURLConnection connection = null;
    try {
      URL url = new URL(updateUrl);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);
      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        in.close();
        JSONObject jsonResponse = (JSONObject) new JSONParser().parse(response.toString());
        return Integer.parseInt(((String) jsonResponse.get("version")).replaceAll("[^0-9]", ""));
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
    return -1;
  }
}
