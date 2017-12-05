package push;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Push {

    public int send(User user, String message) throws Exception {
        String jsonResponse;

        URL url = new URL("https://onesignal.com/api/v1/notifications");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setUseCaches(false);
        con.setDoOutput(true);
        con.setDoInput(true);

        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setRequestProperty("Authorization", "Basic YWFiNmU5MjUtNWFjNi00MTU1LWEzNzMtOGU1ZDBjYjdiNGQz");
        con.setRequestMethod("POST");

        String strJsonBody = "{"
                +   "\"app_id\": \"09dbf63c-a667-4326-b22e-685d6192b100\","
                +   "\"include_player_ids\": [\"" + user.getId() + "\"],"
                +   "\"data\": {\"foo\": \"bar\"},"
                +   "\"contents\": {\"en\": \"" + message + "\"}"
                + "}";

        byte[] sendBytes = strJsonBody.getBytes("UTF-8");
        con.setFixedLengthStreamingMode(sendBytes.length);

        OutputStream outputStream = con.getOutputStream();
        outputStream.write(sendBytes);

        return con.getResponseCode();

    }
    public static void main(String[] args) throws Exception {
        new Push().send(User.jack, "se mi leggi sei frocio");
    }
}
