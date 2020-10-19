/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.gui.helper;

import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import pw.ahs.app.fxsimplecontrols.Dialogs;

import static pw.ahs.app.dleely.Globals.*;

public class AboutHelper {
    private static AboutHelper instance = null;

    public static AboutHelper getInstance() {
        if (instance == null)
            instance = new AboutHelper();
        return instance;
    }

    private AboutHelper() {
    }

    public void showAbout() {
        VBox layout = new VBox();
        Stage stage = Dialogs.createUtilityDialog(view.getStage(), i18n.getString("about.title"), layout);

        ImageView imgIcon = new ImageView("/pw/ahs/app/dleely/res/dleely-icon-32.png");
        imgIcon.setSmooth(true);

        Text txtTitle = new Text(APP_TITLE + " " + APP_VERSION);
        txtTitle.setFont(Font.font("sans-serif", FontWeight.BOLD, 20));
        txtTitle.setEffect(new DropShadow(2, Color.BLACK));

        HBox layoutTitle = new HBox(imgIcon, txtTitle);
        layoutTitle.setSpacing(5);
        layoutTitle.setAlignment(Pos.CENTER);
        layoutTitle.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        layoutTitle.setCursor(Cursor.HAND);
        layoutTitle.setOnMouseClicked(evt -> view.getHostServices().showDocument(APP_HOME));

        Text txtFree = new Text(i18n.getString("about.free-line"));
        txtFree.setTextAlignment(TextAlignment.CENTER);
        Text txtDev = new Text(i18n.getString("about.by-line") + " ");
        Text txtLicense = new Text(i18n.getString("about.license-line"));
        txtLicense.setTextAlignment(TextAlignment.CENTER);

        Hyperlink hlDev = new Hyperlink(i18n.getString("about.name"));
        hlDev.setOnAction(actionEvent -> view.getHostServices().showDocument(DEV_HOME));
        hlDev.requestFocus();

        Button button = new Button(i18n.getString("button.dismiss"));
        button.setOnAction(event -> stage.hide());
        button.setDefaultButton(true);
        button.setCancelButton(true);

        // layout
        TextFlow tflDev = new TextFlow(txtDev, hlDev);
        tflDev.setTextAlignment(TextAlignment.CENTER);

        layout.setSpacing(10);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(layoutTitle, txtFree, tflDev, txtLicense, button);
        layout.setMinHeight(200);
        layout.setMinWidth(300);

        stage.setOnCloseRequest(windowEvent -> {
            windowEvent.consume();
            button.fire();
        });

        stage.show();
    }
}
