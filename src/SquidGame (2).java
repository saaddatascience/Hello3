import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class GreenLightRedLightGame extends Application {

    private boolean isGreenLight = false; // Tracks current light state
    private Text signalText;             // Displays "Green Light" or "Red Light"
    private Timeline lightSwitchTimeline; // Controls light switching
    private Player[] players = new Player[3]; // Array of players
    private boolean gameStarted = false; // Tracks if the game has started
    private Random random = new Random();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, 800, 600);

        // Signal Text
        signalText = new Text("Press Start to Begin");
        signalText.setX(300);
        signalText.setY(50);
        signalText.setStyle("-fx-font: 24 arial;");
        root.getChildren().add(signalText);

        // Start Button
        Button startButton = new Button("Start");
        startButton.setLayoutX(350);
        startButton.setLayoutY(300);
        root.getChildren().add(startButton);

        // Finish Line
        Line finishLine = new Line(700, 100, 700, 500);
        finishLine.setStroke(Color.BLACK);
        finishLine.setStrokeWidth(3);
        root.getChildren().add(finishLine);

        // Create Players
        players[0] = new Player(100, 150, Color.BLUE, root);
        players[1] = new Player(100, 250, Color.RED, root);
        players[2] = new Player(100, 350, Color.GREEN, root);

        // Start Button Action
        startButton.setOnAction(e -> {
            if (!gameStarted) {
                gameStarted = true;
                root.getChildren().remove(startButton); // Remove Start Button
                signalText.setText("Green Light");
                startGame(root);
            }
        });

        primaryStage.setTitle("Green Light Red Light");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startGame(Pane root) {
        // Timeline to switch lights every 3 seconds
        lightSwitchTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> switchLight(root)));
        lightSwitchTimeline.setCycleCount(Timeline.INDEFINITE);
        lightSwitchTimeline.play();

        // Start each player's movement timeline
        for (Player player : players) {
            if (player != null) {
                player.startMovement();
            }
        }
    }

    private void switchLight(Pane root) {
        isGreenLight = !isGreenLight;
        signalText.setText(isGreenLight ? "Green Light" : "Red Light");

        // Check players' status during Red Light
        if (!isGreenLight) {
            for (int i = 0; i < players.length; i++) {
                Player player = players[i];
                if (player != null && player.isMoving()) {
                    // Eliminate player if moving during Red Light
                    root.getChildren().remove(player.getRectangle());
                    players[i] = null;
                    signalText.setText("Player " + (i + 1) + " Eliminated!");
                }
            }

            // Check if there is a winner or if the game is over
            int remainingPlayers = 0;
            for (Player player : players) {
                if (player != null) {
                    remainingPlayers++;
                }
            }

            if (remainingPlayers == 0) {
                signalText.setText("Game Over! All Players Eliminated.");
                lightSwitchTimeline.stop();
            } else if (remainingPlayers == 1) {
                for (int i = 0; i < players.length; i++) {
                    if (players[i] != null) {
                        signalText.setText("Player " + (i + 1) + " Wins!");
                        lightSwitchTimeline.stop();
                        for (Player player : players) {
                            if (player != null) {
                                player.stopMovement();
                            }
                        }
                    }
                }
            }
        }
    }

    // Player Class
    class Player {
        private Rectangle rectangle; // The visual representation of the player
        private double speed;        // Speed of movement
        private Timeline movementTimeline; // Timeline for movement

        public Player(double x, double y, Color color, Pane root) {
            rectangle = new Rectangle(40, 40, color);
            rectangle.setLayoutX(x);
            rectangle.setLayoutY(y);
            root.getChildren().add(rectangle);
            speed = random.nextDouble() * 2 + 1; // Random speed between 1 and 3

            // Create a timeline for independent movement
            movementTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> move()));
            movementTimeline.setCycleCount(Timeline.INDEFINITE);
        }

        public Rectangle getRectangle() {
            return rectangle;
        }

        public void startMovement() {
            movementTimeline.play();
        }

        public void stopMovement() {
            movementTimeline.stop();
        }

        public void move() {
            if (isGreenLight) {
                rectangle.setLayoutX(rectangle.getLayoutX() + speed);

                // Check if the player crosses the finish line
                if (rectangle.getLayoutX() >= 700) {
                    signalText.setText("Player Wins!");
                    lightSwitchTimeline.stop();
                    stopMovement();
                }
            }
        }

        public boolean isMoving() {
            return isGreenLight; // Player is considered moving during Green Light
        }
    }
}
