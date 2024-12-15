import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ButtonRaceGame extends JFrame {
    private static final int FORM_WIDTH = 800;
    private static final int FORM_HEIGHT = 400;
    private static final int BUTTON_HEIGHT = 40;
    private static final int BUTTON_WIDTH = 100;
    private static final int FINISH_LINE = 650;

    private JButton[] buttons;
    private JButton restartButton;
    private ExecutorService executor;
    private volatile boolean raceActive = true;

    public ButtonRaceGame() {
        setTitle("Asphalt 10");
        setSize(FORM_WIDTH, FORM_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        buttons = new JButton[5];
        executor = Executors.newFixedThreadPool(buttons.length);

        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new JButton("Гонщик " + (i + 1));
            buttons[i].setBounds(10, 50 + i * (BUTTON_HEIGHT + 10), BUTTON_WIDTH, BUTTON_HEIGHT);
            add(buttons[i]);
        }

        restartButton = new JButton("Начать новую гонку");
        restartButton.setBounds(300, FORM_HEIGHT - 80, 150, 30);
        restartButton.addActionListener(new RestartActionListener());
        add(restartButton);

        startRace();

        setVisible(true);
    }

    private void startRace() {
        raceActive = true;
        for (JButton button : buttons) {
            executor.submit(new ButtonRunner(button));
        }
    }

    private class ButtonRunner implements Runnable {
        private final JButton button;

        public ButtonRunner(JButton button) {
            this.button = button;
        }

        @Override
        public void run() {
            Random random = new Random();
            while (raceActive && button.getX() < FINISH_LINE) {
                try {
                    Thread.sleep(random.nextInt(50) + 10); // Скорость от 10 до 60 мс
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                if (!raceActive) {
                    return;
                }

                SwingUtilities.invokeLater(() -> {
                    int newX = button.getX() + random.nextInt(10) + 1; // Шаг от 1 до 10
                    button.setBounds(newX, button.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
                    if (newX >= FINISH_LINE && raceActive) {
                        raceActive = false;
                        button.setBackground(Color.GREEN);
                        JOptionPane.showMessageDialog(
                                ButtonRaceGame.this,
                                button.getText() + " победил!",
                                "Гонка окончена",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                });
            }
        }
    }

    private class RestartActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            raceActive = false;
            executor.shutdownNow();
            executor = Executors.newFixedThreadPool(buttons.length);

            for (JButton button : buttons) {
                button.setBounds(10, button.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
                button.setBackground(null);
            }

            startRace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ButtonRaceGame::new);
    }
}
