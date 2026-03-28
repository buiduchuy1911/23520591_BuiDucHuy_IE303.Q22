import java.awt.*;
import java.awt.image.BufferedImage;

public class Pipe {
    int x;
    int y;
    int width;
    int height;
    boolean isTop;
    boolean passed;

    public Pipe(int x, int y, int width, int height, boolean isTop) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isTop = isTop;
        this.passed = false;
    }

    public void moveLeft(int speed) {
        x -= speed;
    }

    public void draw(Graphics2D g2, BufferedImage topPipeImage, BufferedImage bottomPipeImage) {
        if (isTop && topPipeImage != null) {
            g2.drawImage(topPipeImage, x, y, width, height, null);
        } else if (!isTop && bottomPipeImage != null) {
            g2.drawImage(bottomPipeImage, x, y, width, height, null);
        } else {
            g2.setColor(new Color(50, 205, 50));
            g2.fillRect(x, y, width, height);
        }
    }
}
