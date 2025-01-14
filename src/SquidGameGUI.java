import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SquidGameGUI extends Application {

    // List to hold all players
    private List<Circle> players = new ArrayList<>();
    private boolean greenLight = false;  // flag for light color
    private Random random = new Random();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Squid Game - Red Light, Green Light");

        // Set up the canvas
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Create players as circles
        for (int i = 0; i < 10; i++) {
            Circle player = new Circle(20);
            player.setFill(Color.BLUE);
            player.setCenterX(50);
            player.setCenterY(50 + i * 40);  // Space players vertically
            players.add(player);
        }

        // Timeline for Red Light / Green Light
        Timeline gameTimeline = new Timeline(
            new KeyFrame(Duration.seconds(2), e -> switchLight(gc)),
            new KeyFrame(Duration.seconds(1), e -> movePlayers(gc))
        );
        gameTimeline.setCycleCount(Timeline.INDEFINITE);
        gameTimeline.play();

        // Key event to simulate movement during Green Light
        canvas.setOnKeyPressed(this::movePlayerDuringGreenLight);

        // Set up the scene and stage
        canvas.setFocusTraversable(true);
        primaryStage.setScene(new Scene(canvas));
        primaryStage.show();
    }

    private void switchLight(GraphicsContext gc) {
        // Switch between Red and Green Light
        greenLight = !greenLight;

        // Change background color based on light
        if (greenLight) {
            gc.setFill(Color.GREEN);
        } else {
            gc.setFill(Color.RED);
        }
        gc.fillRect(0, 0, 800, 600);  // Fill background with current light color
    }

    private void movePlayers(GraphicsContext gc) {
        // Move players if Green Light is on
        for (Circle player : players) {
            if (greenLight && player.getCenterX() < 750) {
                player.setCenterX(player.getCenterX() + random.nextInt(5));  // Move player
            } else if (!greenLight && player.getCenterX() != 50) {
                // If Red Light and player is moving, remove them
                player.setFill(Color.GRAY);  // Grey out player to indicate elimination
            }
        }
    }

    private void movePlayerDuringGreenLight(KeyEvent event) {
        // Handle key press to simulate player movement during Green Light
        if (greenLight) {
            for (Circle player : players) {
                if (event.getCode().toString().equals("RIGHT")) {
                    if (player.getCenterX() < 750) {
                        player.setCenterX(player.getCenterX() + 10);  // Move player manually with arrow key
                    }
                }
            }
        }
    }
}
