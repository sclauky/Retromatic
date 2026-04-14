package backend;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AccueilController {

    @FXML
    private Pane bgLayer;

    @FXML
    private Button btnEntrer;

    @FXML
    private Circle decoCircleMain;

    @FXML
    private Circle decoCircleSmall;

    @FXML
    private Circle decoRing;

    private final Random random = new Random();
    private final List<Animation> backgroundAnimations = new ArrayList<>();
    private final List<Node> generatedBackgroundNodes = new ArrayList<>();
    private final PauseTransition resizeDebounce = new PauseTransition(javafx.util.Duration.millis(120));

    @FXML
    public void initialize() {
        if (bgLayer == null) {
            return;
        }

        bindDecorativeShapes();

        resizeDebounce.setOnFinished(event -> refreshAnimatedBackground());
        bgLayer.widthProperty().addListener((obs, oldVal, newVal) -> scheduleBackgroundRefresh());
        bgLayer.heightProperty().addListener((obs, oldVal, newVal) -> scheduleBackgroundRefresh());

        Platform.runLater(this::refreshAnimatedBackground);
    }

    private void bindDecorativeShapes() {
        if (decoCircleMain == null || decoCircleSmall == null || decoRing == null) {
            return;
        }

        decoCircleMain.centerXProperty().bind(bgLayer.widthProperty().multiply(0.83));
        decoCircleMain.centerYProperty().bind(bgLayer.heightProperty().multiply(0.15));
        decoCircleMain.radiusProperty().bind(Bindings.min(bgLayer.widthProperty(), bgLayer.heightProperty()).multiply(0.27));

        decoCircleSmall.centerXProperty().bind(bgLayer.widthProperty().multiply(0.12));
        decoCircleSmall.centerYProperty().bind(bgLayer.heightProperty().multiply(0.85));
        decoCircleSmall.radiusProperty().bind(Bindings.min(bgLayer.widthProperty(), bgLayer.heightProperty()).multiply(0.17));

        decoRing.centerXProperty().bind(bgLayer.widthProperty().multiply(0.50));
        decoRing.centerYProperty().bind(bgLayer.heightProperty().multiply(0.50));
        decoRing.radiusProperty().bind(Bindings.min(bgLayer.widthProperty(), bgLayer.heightProperty()).multiply(0.45));
    }

    private void scheduleBackgroundRefresh() {
        if (bgLayer.getWidth() <= 0 || bgLayer.getHeight() <= 0) {
            return;
        }
        resizeDebounce.playFromStart();
    }

    private void refreshAnimatedBackground() {
        if (bgLayer == null || bgLayer.getWidth() <= 0 || bgLayer.getHeight() <= 0) {
            return;
        }

        for (Animation animation : backgroundAnimations) {
            animation.stop();
        }
        backgroundAnimations.clear();

        bgLayer.getChildren().removeAll(generatedBackgroundNodes);
        generatedBackgroundNodes.clear();

        generatedBackgroundNodes.addAll(createFloatingOrbs(bgLayer.getWidth(), bgLayer.getHeight()));
        generatedBackgroundNodes.addAll(createStarField(bgLayer.getWidth(), bgLayer.getHeight()));
        bgLayer.getChildren().addAll(generatedBackgroundNodes);
    }

    private List<javafx.scene.Node> createFloatingOrbs(double width, double height) {
        List<javafx.scene.Node> nodes = new ArrayList<>();
        double minSize = Math.min(width, height);

        nodes.add(createOrb(width * 0.08, height * 0.17, minSize * 0.028, Color.web("#F2EBE0", 0.18), minSize * 0.04, minSize * 0.02, 12));
        nodes.add(createOrb(width * 0.91, height * 0.82, minSize * 0.040, Color.web("#7ECFB3", 0.14), -minSize * 0.05, -minSize * 0.03, 15));
        nodes.add(createOrb(width * 0.84, height * 0.29, minSize * 0.018, Color.web("#E8472A", 0.20), minSize * 0.03, minSize * 0.025, 10));
        nodes.add(createOrb(width * 0.21, height * 0.79, minSize * 0.024, Color.web("#F5C94E", 0.15), -minSize * 0.03, minSize * 0.02, 13));
        return nodes;
    }

    private Circle createOrb(double x, double y, double radius, Color fill, double driftX, double driftY, double seconds) {
        Circle orb = new Circle(radius);
        orb.setCenterX(x);
        orb.setCenterY(y);
        orb.setFill(fill);
        orb.setMouseTransparent(true);

        Timeline drift = new Timeline(
                new KeyFrame(javafx.util.Duration.ZERO,
                        new KeyValue(orb.translateXProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(orb.translateYProperty(), 0, Interpolator.EASE_BOTH)),
                new KeyFrame(javafx.util.Duration.seconds(seconds),
                        new KeyValue(orb.translateXProperty(), driftX, Interpolator.EASE_BOTH),
                        new KeyValue(orb.translateYProperty(), driftY, Interpolator.EASE_BOTH))
        );
        drift.setAutoReverse(true);
        drift.setCycleCount(Animation.INDEFINITE);
        drift.play();
        backgroundAnimations.add(drift);

        FadeTransition glow = new FadeTransition(javafx.util.Duration.seconds(seconds / 2.0 + 1), orb);
        glow.setFromValue(0.55);
        glow.setToValue(1.0);
        glow.setAutoReverse(true);
        glow.setCycleCount(Animation.INDEFINITE);
        glow.play();
        backgroundAnimations.add(glow);

        return orb;
    }

    private List<javafx.scene.Node> createStarField(double width, double height) {
        List<javafx.scene.Node> nodes = new ArrayList<>();
        double minSize = Math.min(width, height);
        double margin = minSize * 0.04;
        for (int i = 0; i < 34; i++) {
            double size = minSize * (0.004 + random.nextDouble() * 0.007);
            double x = margin + random.nextDouble() * Math.max(1, width - (margin * 2));
            double y = margin + random.nextDouble() * Math.max(1, height - (margin * 2));
            Color color = pickStarColor(i);
            nodes.add(createStar(x, y, size, color));
        }
        return nodes;
    }

    private Color pickStarColor(int index) {
        switch (index % 4) {
            case 0:
                return Color.web("#F2EBE0", 0.95);
            case 1:
                return Color.web("#7ECFB3", 0.92);
            case 2:
                return Color.web("#F5C94E", 0.9);
            default:
                return Color.web("#FFFFFF", 0.85);
        }
    }

    private javafx.scene.Node createStar(double x, double y, double size, Color color) {
        Circle glow = new Circle(x, y, size * 1.5, Color.web("#1C1018", 0.20));
        glow.setMouseTransparent(true);

        Polygon star = new Polygon();
        double inner = size * 0.45;
        for (int i = 0; i < 10; i++) {
            double angle = Math.toRadians(-90 + i * 36);
            double radius = (i % 2 == 0) ? size : inner;
            star.getPoints().addAll(
                    x + Math.cos(angle) * radius,
                    y + Math.sin(angle) * radius
            );
        }
        star.setFill(color);
        star.setStroke(Color.web("#1C1018", 0.18));
        star.setStrokeWidth(0.6);
        star.setMouseTransparent(true);

        Timeline drift = new Timeline(
                new KeyFrame(javafx.util.Duration.ZERO,
                        new KeyValue(star.translateYProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(star.rotateProperty(), 0, Interpolator.EASE_BOTH)),
                new KeyFrame(javafx.util.Duration.seconds(6 + random.nextDouble() * 6),
                        new KeyValue(star.translateYProperty(), -6 - random.nextDouble() * 12, Interpolator.EASE_BOTH),
                        new KeyValue(star.rotateProperty(), 180, Interpolator.EASE_BOTH))
        );
        drift.setAutoReverse(true);
        drift.setCycleCount(Animation.INDEFINITE);
        drift.play();
        backgroundAnimations.add(drift);

        FadeTransition twinkle = new FadeTransition(javafx.util.Duration.seconds(1.8 + random.nextDouble() * 2.6), star);
        twinkle.setFromValue(0.35 + random.nextDouble() * 0.25);
        twinkle.setToValue(1.0);
        twinkle.setAutoReverse(true);
        twinkle.setCycleCount(Animation.INDEFINITE);
        twinkle.play();
        backgroundAnimations.add(twinkle);

        return new javafx.scene.Group(glow, star);
    }

    @FXML
    private void handleEntrer() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/frontend/home.fxml"));
        Stage stage = (Stage) btnEntrer.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
    }
}