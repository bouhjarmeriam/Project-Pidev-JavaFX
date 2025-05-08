package service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailSender {

    private static final String EMAIL = "cryptomonnaie95@gmail.com";
    private static final String PASSWORD = "pyjk bqpq kamr xjhz";
 // Password or App Password

    private static Session getSession() {
        // Properties for connecting to Gmail
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // Authentication session
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                System.out.println("Email: " + EMAIL);
                System.out.println("Password: " + PASSWORD);
                return new PasswordAuthentication(EMAIL, PASSWORD);
            }
        });
    }

    public static boolean sendEmail(String to, String subject, String content) {
        if (EMAIL == null || PASSWORD == null || EMAIL.isEmpty() || PASSWORD.isEmpty()) {
            System.out.println("Error: Gmail credentials are not set!");
            return false;
        }

        try {
            // Create the message
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(content);

            // Send the message
            Transport.send(message);
            System.out.println("Email sent successfully to " + to);
            return true;
        } catch (MessagingException e) {
            System.out.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void envoyerEmailInscription(String email, String nom, String password, String type) {
        String subject = "Bienvenue sur ClinicCare!";
        String content = "Bonjour " + nom + ",\n\n" +
                "Bienvenue sur ClinicCare ! Vous êtes inscrit en tant que " + type + ".\n" +
                "Vos identifiants :\n" +
                "Email : " + email + "\n" +
                "Mot de passe : " + password + "\n\n" +
                "Cordialement,\n" +
                "L'équipe ClinicCare.";

        sendEmail(email, subject, content);
    }
}