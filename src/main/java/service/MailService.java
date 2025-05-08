package service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;


public class MailService {

    public static void sendEmail(String toEmail, String subject, String body) {
        final String fromEmail = "cryptomonnaie95@gmail.com"; // Email émetteur
        final String password = "pyjk bqpq kamr xjhz"; // Mot de passe d'application (pas ton vrai mot de passe Gmail !)

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // Serveur SMTP
        props.put("mail.smtp.port", "587"); // Port TLS
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // TLS

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });


        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, "Clinique"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("📩 Email envoyé avec succès !");
        } catch (Exception e) {
            System.err.println("❌ Erreur d'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
        }
    }
    }
