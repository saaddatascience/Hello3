import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GreenLightRedLightGameWithPlayerElimination extends Application {

    private boolean isGreenLight = false; // Tracks current light state
    private Text signalText;             // Displays "Green Light" or "Red Light"
    private Timeline lightSwitchTimeline; // Controls light switching
    private Timeline gameTimer;           // Timer for the game
    private Player[] players = new Player[3]; // Array of players
    private boolean gameStarted = false; // Tracks if the game has started
    private Random random = new Random();
    private ImageView doll;              // The doll image
    private Text timerText;              // Timer display
    private int remainingTime = 180;     // Game duration in seconds (3 minutes)
    private List<Player> leaderboard = new ArrayList<>(); // Tracks players who finished

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

        // Timer Text
        timerText = new Text("Time Left: 180s");
        timerText.setX(600);
        timerText.setY(50);
        timerText.setStyle("-fx-font: 24 arial;");
        root.getChildren().add(timerText);

        // Doll Image
        try {
            Image dollImage = new Image("file:F:/Hello3/src/image/doll.jpg"); // Using the provided path
            doll = new ImageView(dollImage);
            doll.setFitWidth(100);
            doll.setFitHeight(150);
            doll.setLayoutX(700 - (doll.getFitWidth() / 2)); // Align with the finish line's X-coordinate
            doll.setLayoutY(250); // Center vertically around the finish line
            doll.setRotate(0); // Face towards the players initially
            root.getChildren().add(doll);
        } catch (Exception e) {
            signalText.setText("Error loading doll image!");
            e.printStackTrace();
        }

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
        players[0] = new Player(100, 150, Color.BLUE, root, "Player 1");
        players[1] = new Player(100, 250, Color.RED, root, "Player 2");
        players[2] = new Player(100, 350, Color.GREEN, root, "Player 3");

        // Start Button Action
        startButton.setOnAction(e -> {
            if (!gameStarted) {
                gameStarted = true;
                root.getChildren().remove(startButton); // Remove Start Button
                signalText.setText("Green Light");
                startGame(root);
            }
        });

        primaryStage.setTitle("Green Light Red Light with Player Elimination");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startGame(Pane root) {
        // Start the game timer
        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer(root)));
        gameTimer.setCycleCount(remainingTime);
        gameTimer.play();

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

    private void updateTimer(Pane root) {
        remainingTime--;
        timerText.setText("Time Left: " + remainingTime + "s");

        if (remainingTime <= 0) {
            // Time is up, eliminate all remaining players
            gameTimer.stop();
            lightSwitchTimeline.stop();
            for (int i = 0; i < players.length; i++) {
                Player player = players[i];
                if (player != null) {
                    root.getChildren().remove(player.getRectangle());
                    players[i] = null; // Remove player reference
                }
            }
            signalText.setText("Time's Up! All Players Eliminated.");
            showLeaderboard(root);
        }
    }

    private void switchLight(Pane root) {
        isGreenLight = !isGreenLight;
        signalText.setText(isGreenLight ? "Green Light" : "Red Light");

        // Rotate Doll to indicate light change
        RotateTransition rotateDoll = new RotateTransition(Duration.seconds(1), doll);
        rotateDoll.setToAngle(isGreenLight ? 0 : 180); // Face towards players (0) or away (180)
        rotateDoll.play();

        // Check players' status during Red Light
        if (!isGreenLight) {
            for (int i = 0; i < players.length; i++) {
                Player player = players[i];
                if (player != null && player.isMoving()) {
                    // Eliminate player if moving during Red Light
                    root.getChildren().remove(player.getRectangle());
                    players[i] = null;
                    signalText.setText(player.getName() + " Eliminated!");
                }
            }
        }
    }

    private void showLeaderboard(Pane root) {
        StringBuilder leaderboardText = new StringBuilder("Leaderboard:\n");
        int rank = 1;

        for (Player player : leaderboard) {
            leaderboardText.append(rank).append(". ").append(player.getName()).append("\n");
            rank++;
        }

        Text leaderboardDisplay = new Text(300, 400, leaderboardText.toString());
        leaderboardDisplay.setStyle("-fx-font: 20 arial;");
        root.getChildren().add(leaderboardDisplay);
    }

    // Player Class
    class Player {
        private Rectangle rectangle; // The visual representation of the player
        private double speed;        // Speed of movement
        private Timeline movementTimeline; // Timeline for movement
        private String name;         // Player's name

        public Player(double x, double y, Color color, Pane root, String name) {
            rectangle = new Rectangle(40, 40, color);
            rectangle.setLayoutX(x);
            rectangle.setLayoutY(y);
            root.getChildren().add(rectangle);

            // Adjust speed for faster movement
            speed = random.nextDouble() * 4 + 3; // Random speed between 3 and 7
            this.name = name;

            movementTimeline = new Timeline(new KeyFrame(Duration.millis(random.nextDouble() * 1000 + 500), e -> move()));
            movementTimeline.setCycleCount(Timeline.INDEFINITE);
        }

        public Rectangle getRectangle() {
            return rectangle;
        }

        public String getName() {
            return name;
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

                if (rectangle.getLayoutX() >= 700) {
                    signalText.setText(name + " Finished!");
                    leaderboard.add(this);
                    stopMovement();
                }
            }
        }

        public boolean isMoving() {
            return isGreenLight; // Player is considered moving during Green Light
        }
    }
}
