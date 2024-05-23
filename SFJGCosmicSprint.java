import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;

public class SFJGCosmicSprint {

	public static Point loc;
	public static int score, speed=3, lost = 0;
	public static GWindow window = null;
	public static GScreen screen = null;
	public static GThread gameLoop = null;
	public static List<Point> bullets;
	public static List<Point> asteroids;

	public static class GWindow extends JFrame {

		public GWindow() {
			setTitle("Single File Java Game: Cosmic Sprint");
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setMinimumSize(new Dimension(400,400));
		}

	}

	public static class GScreen extends Canvas implements KeyListener {

		private Color bulletColor;

		public GScreen() {
			setBackground(Color.black);
			bulletColor = new Color(0, 200, 0);
			setFocusable(true);
			requestFocus();
			addKeyListener(this);
		}

		public void createBufferStrategy() {
			createBufferStrategy(2);
		}


		public void render() {
			BufferStrategy bs = getBufferStrategy();
			if (bs == null) return;
			Graphics g = bs.getDrawGraphics();
			draw(g);
			g.dispose();
			bs.show();
		}

		public void draw(Graphics g) {
			g.setColor(new Color(0, 0, 0, 50));
			g.fillRect(0, 0, getWidth(), getHeight());
			if (gameLoop == null) return;
			g.setColor(Color.green);
			g.drawString("FPS: " + gameLoop.fps + " | Score: " + score, 10, 40);
			if (lost == -1) {
				g.setColor(Color.red);
				g.setFont(new Font("Bold", Font.BOLD, 50));
				g.drawString("You lost!", getWidth() / 2, 200);
				g.drawString("Your score was: ", getWidth() / 2, 250);
				g.drawString(score + "", getWidth() / 2, 300);
			} else {
				g.setColor(Color.blue);
				g.fillRect(loc.x, loc.y, 20, 10);
				int index = 0;

				g.setColor(bulletColor);
				List<Point> to_removal = new ArrayList<>();
				for (index = 0; index < bullets.size(); index++) {
					final Point b = bullets.get(index);
					g.fillRoundRect(
							b.x, b.y,
							10, 5,
							10, 5
					);
					Point ast = asteroids.stream().filter(a -> ((b.x >= a.x) && (b.x + 10 <= a.x + 30)) && ((b.y >= a.y) && (b.y + 5 <= a.y + 30))).findFirst().orElse(new Point(-100, -100));
					if (!ast.equals(new Point(-100, -100))) {
						asteroids.remove(ast);
						score++;
						to_removal.add(b);
					}
					if (b.x > getWidth()) to_removal.add(b);
					b.x += Math.pow(speed, 3);
					System.out.println(b.x);
				}
				g.setColor(Color.lightGray);
				for (index = 0; index < asteroids.size(); index++) {
					g.fillRect(asteroids.get(index).x, asteroids.get(index).y, 30, 30);
					asteroids.get(index).x -= speed;
					if (asteroids.get(index).x < 0) lost = -1;
				}

				bullets.removeAll(to_removal);
			}

		}

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (lost == -1) reset();
			switch (e.getKeyCode()) {
				case KeyEvent.VK_W -> {
					loc.y -= speed * 2;
				}
				case KeyEvent.VK_S -> {
					loc.y += speed * 2;
				}
				case KeyEvent.VK_SPACE -> {
					if (bullets.size() < 5) bullets.add(loc.getLocation());
				}
				default -> {

				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}
	}

	public static class GThread extends Thread {

		public int fps;
		public long start;

		public GThread() {
			fps = 0;
		}

		@Override
		public void start() {
			super.start();
			start = System.currentTimeMillis();
			while (window.isVisible()) {
				fps++;
				screen.render();
				if (System.currentTimeMillis()-start >= 2000) {
					fps = 0;
					start = System.currentTimeMillis();
					if (asteroids.size() < Math.max(Math.pow(score-2,2),1) ) {
						asteroids.add(
								new Point(
										window.getWidth() - 20,
										(int) (30 + Math.random() * (window.getHeight()/2-30))
								)
						);
					}
				}
				try {
					Thread.sleep(16); // To achieve roughly 60 FPS
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		bullets = new ArrayList<>();
		asteroids = new ArrayList<>();
		loc = new Point(10,200);
		score = 0;
		window = new GWindow();
		screen = new GScreen();
		gameLoop = new GThread();
		window.add(screen);
		window.setVisible(true);
		screen.createBufferStrategy();
		lost = 0;
		gameLoop.start();
	}

	public static void reset() {
		bullets = new ArrayList<>();
		asteroids = new ArrayList<>();
		loc = new Point(10,200);
		score = 0;
		lost = 0;
	}

}
