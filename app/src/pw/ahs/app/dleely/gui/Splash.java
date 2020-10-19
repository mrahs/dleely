/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.gui;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.Collection;

public class Splash {

    private static Stage stage = null;

    public static void hide() {
        if (stage != null)
            stage.hide();
    }

    public static void show() {
        if (stage != null)
            stage.show();
    }

    public static void updateSplashIcons(Collection<Image> icons) {
        if (stage != null)
            stage.getIcons().setAll(icons);
    }

    public static void show(String title, Image image, Collection<Image> icons) {
        stage = new Stage(StageStyle.TRANSPARENT);
        stage.setTitle(title);
        stage.getIcons().setAll(icons);

        // effect
        DropShadow dropShadow = new DropShadow(35, Color.WHITE);
        dropShadow.setBlurType(BlurType.GAUSSIAN);
        dropShadow.setSpread(.6);

        // content
        Text text = new Text(title);
        text.setFont(Font.font("Sans-Serif", FontWeight.BLACK, 100));
        text.setFill(Color.rgb(77, 77, 77));
        text.setEffect(dropShadow);

        ImageView imageView = new ImageView(image);
        imageView.setEffect(dropShadow);

        // layout
        HBox layout = new HBox(imageView, text);
        layout.setAlignment(Pos.CENTER);
        layout.setPrefSize(600, 400);
        layout.setStyle("-fx-background-color: transparent;");
        layout.setPickOnBounds(false);

        // drag
        DoubleProperty xOffset = new SimpleDoubleProperty(0);
        DoubleProperty yOffset = new SimpleDoubleProperty(0);
        text.setOnMousePressed(evt -> {
            xOffset.set(evt.getScreenX());
            yOffset.set(evt.getScreenY());
        });
        imageView.setOnMousePressed(text.getOnMousePressed());
        text.setOnMouseDragged(evt -> {
            Window window = layout.getScene().getWindow();
            double deltaX = evt.getScreenX() - xOffset.get();
            double deltaY = evt.getScreenY() - yOffset.get();
            window.setX(window.getX() + deltaX);
            window.setY(window.getY() + deltaY);

            xOffset.set(evt.getScreenX());
            yOffset.set(evt.getScreenY());
        });
        imageView.setOnMouseDragged(text.getOnMouseDragged());

        // animation
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.setAutoReverse(true);
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(0), new KeyValue(dropShadow.radiusProperty(), 35, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(1000), new KeyValue(dropShadow.radiusProperty(), 75, Interpolator.EASE_BOTH))
        );
        timeline.play();

        Scene scene = new Scene(layout);
        scene.setFill(Color.TRANSPARENT);
        scene.getAccelerators().put(KeyCombination.valueOf("ESC"), stage::hide);
        scene.setOnMouseClicked(evt -> {
            if (evt.getButton() == MouseButton.SECONDARY) stage.hide();
        });

        stage.setScene(scene);
        stage.show();
    }
}
