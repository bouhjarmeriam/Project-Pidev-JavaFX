package service;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import java.util.regex.Pattern;

public class TwilioSMSService {
    private final String accountSid;
    private final String authToken;
    private final String twilioNumber;
    private static final Pattern TUNISIAN_PATTERN = Pattern.compile("^\\+216[2459]\\d{7}$");

    public TwilioSMSService() {
        Properties props = loadConfig();
        this.accountSid = props.getProperty("twilio.account.sid");
        this.authToken = props.getProperty("twilio.auth.token");
        this.twilioNumber = props.getProperty("twilio.phone.number");
    }

    public boolean isConfigured() {
        return accountSid != null && !accountSid.isEmpty() &&
                authToken != null && !authToken.isEmpty() &&
                twilioNumber != null && !twilioNumber.isEmpty();
    }

    public String sendSMS(String toNumber, String message) {
        if (!isConfigured()) {
            return "Twilio not configured properly";
        }

        try {
            String formattedNumber = formatPhoneNumber(toNumber);
            if (!isValidPhoneNumber(formattedNumber)) {
                return "Invalid phone number format for " + toNumber;
            }

            String apiUrl = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
            String authString = accountSid + ":" + authToken;
            String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));

            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            String postData = String.format(
                    "To=%s&From=%s&Body=%s",
                    URLEncoder.encode(formattedNumber, "UTF-8"),
                    URLEncoder.encode(twilioNumber, "UTF-8"),
                    URLEncoder.encode(message, "UTF-8")
            );

            try (OutputStream os = connection.getOutputStream()) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                return "SMS sent successfully to " + formattedNumber;
            } else {
                return parseErrorResponse(connection);
            }
        } catch (Exception e) {
            return "SMS sending failed: " + e.getMessage();
        }
    }

    private String formatPhoneNumber(String phoneNumber) throws IllegalArgumentException {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        String formatted = phoneNumber.replaceAll("[^0-9+]", "");

        if (!formatted.startsWith("+")) {
            if (formatted.startsWith("216")) {
                formatted = "+" + formatted;
            } else if (formatted.startsWith("0")) {
                formatted = "+216" + formatted.substring(1);
            } else {
                formatted = "+" + formatted;
            }
        }

        return formatted;
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return false;

        if (phoneNumber.startsWith("+216")) {
            return TUNISIAN_PATTERN.matcher(phoneNumber).matches();
        }

        return false;
    }

    private String parseErrorResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return "Twilio API Error: " + response.toString();
        }
    }

    private Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException e) {
            System.err.println("Error loading config: " + e.getMessage());
        }
        return props;
    }
}