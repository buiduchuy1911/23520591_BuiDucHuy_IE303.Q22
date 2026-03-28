import java.awt.*;
import java.awt.image.BufferedImage;

public class Bird {
    int x;
    double y;
    int width;
    int height;
    double velocityY;

    public Bird(int x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.velocityY = 0;
    }

    public void update(double gravity) {
        velocityY += gravity;
        y += velocityY;
    }

    public void jump(double jumpForce) {
        velocityY = jumpForce;
    }

    public void draw(Graphics2D g2, BufferedImage birdImage) {
        int drawY = (int) Math.round(y);
        if (birdImage != null) {
            g2.drawImage(birdImage, x, drawY, width, height, null);
            return;
        }

        g2.setColor(Color.YELLOW);
        g2.fillOval(x, drawY, width, height);
    }

    public boolean intersects(Pipe pipe) {
        return x < pipe.x + pipe.width
            && x + width > pipe.x
            && y < pipe.y + pipe.height
            && y + height > pipe.y;
    }
}
