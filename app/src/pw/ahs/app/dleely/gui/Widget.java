/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.gui;

import javafx.animation.*;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import pw.ahs.app.dleely.Globals;

import java.util.Collection;

public class Widget {
    private final Stage stage;

    public Widget(Collection<Image> icons) {
        stage = new Stage(StageStyle.TRANSPARENT);
        stage.setTitle(Globals.APP_TITLE);
        stage.getIcons().setAll(icons);
        stage.setResizable(false);


        Text text = new Text("D");

        final double radiusNormal = 25;
        final String textStyleNormal = "-fx-font: bold 2em serif; -fx-fill: white";
        text.setStyle(textStyleNormal);
        Circle circle = new Circle(radiusNormal, Color.SILVER);
        StackPane stackPane = new StackPane(circle, text);
        stackPane.setStyle("-fx-background-color: transparent");
        stackPane.setOpacity(0.5);

        DoubleProperty xOffset = new SimpleDoubleProperty();
        DoubleProperty yOffset = new SimpleDoubleProperty();
        stackPane.setOnMousePressed(evt -> {
            xOffset.set(evt.getScreenX());
            yOffset.set(evt.getScreenY());
        });
        stackPane.setOnMouseDragged(evt -> {
            stage.setX(stage.getX() + evt.getScreenX() - xOffset.get());
            stage.setY(stage.getY() + evt.getScreenY() - yOffset.get());
            xOffset.set(evt.getScreenX());
            yOffset.set(evt.getScreenY());
        });

        TextField tfName = new TextField();
        tfName.setPromptText("Name");
        TextField tfRef = new TextField();
        tfRef.setPromptText("Ref");
        TextField tfInfo = new TextField();
        tfInfo.setPromptText("Info");
        TextField tfTags = new TextField();
        tfTags.setPromptText("Tags");
        ToggleButton tbPrivacy = new ToggleButton("Public");
        tbPrivacy.textProperty().bind(new StringBinding() {
            {
                super.bind(tbPrivacy.selectedProperty());
            }

            @Override
            protected String computeValue() {
                return tbPrivacy.isSelected() ? "Private" : "Public";
            }
        });
        tbPrivacy.setMaxWidth(Double.MAX_VALUE);

        VBox layout = new VBox(tfName, tfRef, tfInfo, tfTags, tbPrivacy);
//        layout.setManaged(false);
//        layout.setVisible(false);
        layout.setStyle("-fx-background-color: transparent");
        layout.setPrefWidth(300);

        AnchorPane anchorPane = new AnchorPane(stackPane, layout);
        AnchorPane.setTopAnchor(stackPane, 0d);
        AnchorPane.setLeftAnchor(stackPane, 0d);
        AnchorPane.setTopAnchor(layout, 25d);
        AnchorPane.setLeftAnchor(layout, 25d);
        anchorPane.setStyle("-fx-background-color: transparent");

        Scene scene = new Scene(anchorPane, Color.TRANSPARENT);

        stage.setScene(scene);

        RotateTransition rotateInTransition = new RotateTransition(Duration.millis(500), stackPane);
        rotateInTransition.setFromAngle(0);
        rotateInTransition.setToAngle(360);
        RotateTransition rotateOutTransition = new RotateTransition(Duration.millis(500), stackPane);
        rotateOutTransition.setFromAngle(360);
        rotateOutTransition.setToAngle(0);

        Timeline fadeInTranstion = new Timeline(
                new KeyFrame(Duration.millis(500), new KeyValue(stackPane.opacityProperty(), 1))
        );
        Timeline fadeOutTransition = new Timeline(
                new KeyFrame(Duration.millis(500), new KeyValue(stackPane.opacityProperty(), .5))
        );

        Timeline slideInTranslation = new Timeline(
                new KeyFrame(Duration.millis(100), new KeyValue(tfName.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(200), new KeyValue(tfRef.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(300), new KeyValue(tfInfo.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(400), new KeyValue(tfTags.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(500), new KeyValue(tbPrivacy.translateXProperty(), 0))
        );

        Timeline slideOutTranslation = new Timeline(
                new KeyFrame(Duration.millis(100), new KeyValue(tfName.translateXProperty(), -500)),
                new KeyFrame(Duration.millis(200), new KeyValue(tfRef.translateXProperty(), -500)),
                new KeyFrame(Duration.millis(300), new KeyValue(tfInfo.translateXProperty(), -500)),
                new KeyFrame(Duration.millis(400), new KeyValue(tfTags.translateXProperty(), -500)),
                new KeyFrame(Duration.millis(500), new KeyValue(tbPrivacy.translateXProperty(), -500))
        );

        ParallelTransition inTransition = new ParallelTransition(
                rotateInTransition,
                fadeInTranstion,
                slideInTranslation
        );

        ParallelTransition outTransition = new ParallelTransition(
                rotateOutTransition,
                fadeOutTransition,
                slideOutTranslation
        );

        Runnable setIdle = () -> outTransition.playFromStart();
        Runnable setActive = () -> inTransition.playFromStart();
        stackPane.setOnMouseEntered(evt -> setActive.run());
        scene.setOnMouseExited(evt -> {
            if (tfName.getText().trim().isEmpty()
                    && tfRef.getText().trim().isEmpty()
                    && tfInfo.getText().trim().isEmpty()
                    && tfTags.getText().trim().isEmpty())
                setIdle.run();
        });

        stackPane.setOnDragOver(evt -> {
            if (((DragEvent) evt).getDragboard().hasString()) {
                evt.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
        });
        stackPane.setOnDragDropped(evt -> {
            tfRef.setText(evt.getDragboard().getString());
            setActive.run();
        });
        scene.getAccelerators().put(KeyCombination.valueOf("ESCAPE"), setIdle);
        setIdle.run();
    }

    public void show() {
        stage.show();
    }

    public void hide() {
        stage.hide();
    }
}
