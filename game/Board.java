package game;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

@SuppressWarnings("serial")
public class Board extends JPanel {

    private Timer timer;
    private String message = "Game Over";
    private Ball Ball;
    private Paddle Paddle;
    private Brick[] Bricks;
    private boolean inGame = true;

    public Board() {

        initBoard();
    }

    private void initBoard() {

        addKeyListener(new TAdapter());
        setFocusable(true);
        setPreferredSize(new Dimension(Commons.WIDTH, Commons.HEIGHT));

        gameInit();
    }

    private void gameInit() {

        Bricks = new Brick[Commons.N_OF_BRICKS];

        Ball = new Ball();
        Paddle = new Paddle();

        int k = 0;

        for (int i = 0; i < 5; i++) {

            for (int j = 0; j < 6; j++) {

                Bricks[k] = new Brick(j * 40 + 30, i * 10 + 50);
                k++;
            }
        }

        timer = new Timer(Commons.PERIOD, new GameCycle());
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        var g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        if (inGame) {

            drawObjects(g2d);
        } else {

            gameFinished(g2d);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawObjects(Graphics2D g2d) {

        g2d.drawImage(Ball.getImage(), Ball.getX(), Ball.getY(),
                Ball.getImageWidth(), Ball.getImageHeight(), this);
        g2d.drawImage(Paddle.getImage(), Paddle.getX(), Paddle.getY(),
                Paddle.getImageWidth(), Paddle.getImageHeight(), this);

        for (int i = 0; i < Commons.N_OF_BRICKS; i++) {

            if (!Bricks[i].isDestroyed()) {

                g2d.drawImage(Bricks[i].getImage(), Bricks[i].getX(),
                        Bricks[i].getY(), Bricks[i].getImageWidth(),
                        Bricks[i].getImageHeight(), this);
            }
        }
    }

    private void gameFinished(Graphics2D g2d) {

        var font = new Font("Verdana", Font.BOLD, 18);
        FontMetrics fontMetrics = this.getFontMetrics(font);

        g2d.setColor(Color.BLACK);
        g2d.setFont(font);
        g2d.drawString(message, (Commons.WIDTH - fontMetrics.stringWidth(message)) / 2, Commons.WIDTH / 2);
    }

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {

            Paddle.keyReleased(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {

            Paddle.keyPressed(e);
        }
    }

    private class GameCycle implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            doGameCycle();
        }
    }

    private void doGameCycle() {

        Ball.move();
        Paddle.move();
        checkCollision();
        repaint();
    }

    private void stopGame() {

        inGame = false;
        timer.stop();
    }

    private void checkCollision() {

        if (Ball.getRect().getMaxY() > Commons.BOTTOM_EDGE) {

            stopGame();
        }

        for (int i = 0, j = 0; i < Commons.N_OF_BRICKS; i++) {

            if (Bricks[i].isDestroyed()) {

                j++;
            }

            if (j == Commons.N_OF_BRICKS) {

                message = "Congratulations!";
                stopGame();
            }
        }

        if ((Ball.getRect()).intersects(Paddle.getRect())) {

            int paddleLPos = (int) Paddle.getRect().getMinX();
            int ballLPos = (int) Ball.getRect().getMinX();

            int first = paddleLPos + 8;
            int second = paddleLPos + 16;
            int third = paddleLPos + 24;
            int fourth = paddleLPos + 32;

            if (ballLPos < first) {

                Ball.setXDir(-1);
                Ball.setYDir(-1);
            }

            if (ballLPos >= first && ballLPos < second) {

                Ball.setXDir(-1);
                Ball.setYDir(-1 * Ball.getYDir());
            }

            if (ballLPos >= second && ballLPos < third) {

                Ball.setXDir(0);
                Ball.setYDir(-1);
            }

            if (ballLPos >= third && ballLPos < fourth) {

                Ball.setXDir(1);
                Ball.setYDir(-1 * Ball.getYDir());
            }

            if (ballLPos > fourth) {

                Ball.setXDir(1);
                Ball.setYDir(-1);
            }
        }

        for (int i = 0; i < Commons.N_OF_BRICKS; i++) {

            if ((Ball.getRect()).intersects(Bricks[i].getRect())) {

                int ballLeft = (int) Ball.getRect().getMinX();
                int ballHeight = (int) Ball.getRect().getHeight();
                int ballWidth = (int) Ball.getRect().getWidth();
                int ballTop = (int) Ball.getRect().getMinY();

                var pointRight = new Point(ballLeft + ballWidth + 1, ballTop);
                var pointLeft = new Point(ballLeft - 1, ballTop);
                var pointTop = new Point(ballLeft, ballTop - 1);
                var pointBottom = new Point(ballLeft, ballTop + ballHeight + 1);

                if (!Bricks[i].isDestroyed()) {

                    if (Bricks[i].getRect().contains(pointRight)) {

                        Ball.setXDir(-1);
                    } else if (Bricks[i].getRect().contains(pointLeft)) {

                        Ball.setXDir(1);
                    }

                    if (Bricks[i].getRect().contains(pointTop)) {

                        Ball.setYDir(1);
                    } else if (Bricks[i].getRect().contains(pointBottom)) {

                        Ball.setYDir(-1);
                    }

                    Bricks[i].setDestroyed(true);
                }
            }
        }
    }
}