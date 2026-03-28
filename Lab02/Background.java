import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class Background {
    private final int width;
    private final int height;
    private final BufferedImage backgroundImage;

    public Background(int width, int height, String imagePath) {
        this.width = width;
        this.height = height;
        this.backgroundImage = loadImage(imagePath);
    }

    public void draw(Graphics2D g2) {
        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, width, height, null);
            return;
        }

        g2.setColor(new Color(115, 208, 255));
        g2.fillRect(0, 0, width, height);
    }

    public void drawGround(Graphics2D g2, int groundHeight) {
        g2.setColor(new Color(222, 199, 141));
        g2.fillRect(0, height - groundHeight, width, groundHeight);
    }

    private BufferedImage loadImage(String fileName) {
        try {
            return ImageIO.read(new File(fileName));
        } catch (IOException e) {
            return null;
        }
    }
}
