package entite;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class CaptchaGenerator {
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int CAPTCHA_LENGTH = 5;

    public static String generateCaptchaText() {
        StringBuilder captcha = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            captcha.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return captcha.toString();
    }

    public static File generateCaptchaImage(String captchaText) throws IOException {
        int width = 150, height = 50;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 25));

        for (int i = 0; i < captchaText.length(); i++) {
            g.drawString(String.valueOf(captchaText.charAt(i)), 20 + (i * 20), 35);
        }

        g.dispose();

        File file = new File("captcha.png");
        ImageIO.write(image, "png", file);
        return file;
    }

    public static Image getCaptchaImage(String text) throws IOException {
        return SwingFXUtils.toFXImage(ImageIO.read(generateCaptchaImage(text)), null);
    }
}

