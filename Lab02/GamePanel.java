import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 360;
    private static final int HEIGHT = 640;

    private static final int GROUND_HEIGHT = 70;
    private static final int BIRD_WIDTH = 42;
    private static final int BIRD_HEIGHT = 30;
    private static final int PIPE_WIDTH = 60;
    private static final int PIPE_GAP = 160;
    private static final int PIPE_SPEED = 3;

    private static final double GRAVITY = 0.45;
    private static final double JUMP_FORCE = -7.5;

    private final Timer timer;
    private final Random random = new Random();
    private final List<Pipe> pipes = new ArrayList<>();
    private final Background background;

    private final BufferedImage birdImage;
    private final BufferedImage topPipeImage;
    private final BufferedImage bottomPipeImage;

    private Bird bird;
    private int pipeSpawnCounter;
    private int score;
    private boolean gameOver;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        background = new Background(WIDTH, HEIGHT, "flappybirdbg.png");
        birdImage = loadImage("flappybird.png");
        topPipeImage = loadImage("toppipe.png");
        bottomPipeImage = loadImage("bottompipe.png");

        resetGame();

        timer = new Timer(1000 / 60, this);
        timer.start();
    }

    private BufferedImage loadImage(String fileName) {
        try {
            return ImageIO.read(new File(fileName));
        } catch (IOException e) {
            return null;
        }
    }

    private void resetGame() {
        bird = new Bird(70, HEIGHT / 2.0 - 50, BIRD_WIDTH, BIRD_HEIGHT);
        pipes.clear();
        score = 0;
        gameOver = false;
        pipeSpawnCounter = 0;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    private void updateGame() {
        if (gameOver) {
            return;
        }

        bird.update(GRAVITY);

        pipeSpawnCounter++;
        if (pipeSpawnCounter >= 90) {
            pipeSpawnCounter = 0;
            spawnPipePair();
        }

        Iterator<Pipe> iterator = pipes.iterator();
        while (iterator.hasNext()) {
            Pipe pipe = iterator.next();
            pipe.moveLeft(PIPE_SPEED);

            if (!pipe.passed && !pipe.isTop && pipe.x + pipe.width < bird.x) {
                pipe.passed = true;
                score++;
            }

            if (pipe.x + pipe.width < 0) {
                iterator.remove();
            }
        }

        checkCollisions();
    }

    private void spawnPipePair() {
        int minTopHeight = 80;
        int maxTopHeight = HEIGHT - GROUND_HEIGHT - PIPE_GAP - 120;
        int topHeight = minTopHeight + random.nextInt(Math.max(1, maxTopHeight - minTopHeight));
        int bottomY = topHeight + PIPE_GAP;
        int bottomHeight = HEIGHT - GROUND_HEIGHT - bottomY;

        pipes.add(new Pipe(WIDTH, 0, PIPE_WIDTH, topHeight, true));
        pipes.add(new Pipe(WIDTH, bottomY, PIPE_WIDTH, bottomHeight, false));
    }

    private void checkCollisions() {
        if (bird.y < 0) {
            bird.y = 0;
            bird.velocityY = 0;
        }

        if (bird.y + bird.height >= HEIGHT - GROUND_HEIGHT) {
            bird.y = HEIGHT - GROUND_HEIGHT - bird.height;
            gameOver = true;
            return;
        }

        for (Pipe pipe : pipes) {
            if (bird.intersects(pipe)) {
                gameOver = true;
                return;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        background.draw(g2);
        drawPipes(g2);
        bird.draw(g2, birdImage);
        background.drawGround(g2, GROUND_HEIGHT);
        drawScore(g2);

        if (gameOver) {
            drawGameOver(g2);
        }

        g2.dispose();
    }

    private void drawPipes(Graphics2D g2) {
        for (Pipe pipe : pipes) {
            pipe.draw(g2, topPipeImage, bottomPipeImage);
        }
    }

    private void drawScore(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 36));
        String scoreText = String.valueOf(score);
        int textWidth = g2.getFontMetrics().stringWidth(scoreText);
        g2.drawString(scoreText, (WIDTH - textWidth) / 2, 60);
    }

    private void drawGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 42));
        String text = "Game Over";
        int textWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (WIDTH - textWidth) / 2, HEIGHT / 2 - 20);

        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        String restartText = "Nhan Enter/Space de choi lai";
        int restartWidth = g2.getFontMetrics().stringWidth(restartText);
        g2.drawString(restartText, (WIDTH - restartWidth) / 2, HEIGHT / 2 + 25);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_ENTER) {
            if (gameOver) {
                resetGame();
            } else {
                bird.jump(JUMP_FORCE);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
