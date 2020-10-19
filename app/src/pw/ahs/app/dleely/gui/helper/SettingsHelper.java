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
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pw.ahs.app.dleely.controller.Controller;
import pw.ahs.app.dleely.gui.UIHelper;
import pw.ahs.app.fxsimplecontrols.Dialogs;

import static pw.ahs.app.dleely.Globals.*;
import static pw.ahs.app.dleely.controller.SettingsController.Setting.*;

public class SettingsHelper {
    private static SettingsHelper instance = null;

    public static SettingsHelper getInstance() {
        if (instance == null)
            instance = new SettingsHelper();
        return instance;
    }

    private SettingsHelper() {

    }

    public void init() {
        Controller.beforeExit.addAction(this::save);

        /* applying settings */

        // reopen last file and
        // file path and
        // check for update are applied on startup

        // confirm delete is applied at operation

        // remember file path
        Controller.settings.addListener(sRemember_Opened_Files, (oldValue, newValue)
                -> UIHelper.menu.setOpenRecentMenuVisible(Boolean.parseBoolean(newValue)));
        // language
        Controller.settings.addListener(sLang, (oldValue, newValue)
                -> Controller.curLang.setLang(newValue));
    }

//    public void apply() {
        // reopen last file and
        // file path is applied on startup

        // remember file path
//        UIHelper.menu.setOpenRecentMenuVisible(Controller.settings.getSettingBoolean(sRemember_Opened_Files));

        // language
//        Controller.curLang.setLang(Controller.settings.getSetting(sLang));

        // check for update
        // confirm delete
//    }

    public void save() {
        // file path
        if (!Controller.settings.getSettingBoolean(sRemember_Opened_Files)) {
            Controller.settings.restoreToDefault(sFile_Path);
        }

        // remember file paths
        // reopen last file
        // language
        // check for update
        // confirm delete

        // save
        if (!Controller.settings.save(PREFS_FILE_NAME))
            Dialogs.showMessageDialog(
                    view.getStage(),
                    i18n.getString("settings.error.title"),
                    i18n.getString("settings.error.write-msg"),
                    i18n.getString("button.dismiss")
            );
    }

    public void show() {
        VBox layout = new VBox();
        Stage stage = Dialogs.createUtilityDialog(view.getStage(), i18n.getString("settings.title"), layout);

        TextField tfCurrentFile = new TextField();
        if (UIHelper.io.isFileOpen())
            tfCurrentFile.setText(Controller.settings.getSetting(sFile_Path).split(";")[0]);
        tfCurrentFile.setEditable(false);
        tfCurrentFile.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        Label lblCurrentFile = new Label(i18n.getString("settings.current-file"));
        lblCurrentFile.setLabelFor(tfCurrentFile);

        ChoiceBox<String> cbLang = new ChoiceBox<>();
        cbLang.getItems().addAll("English", "العربية");
        cbLang.getSelectionModel().select(THE_LANG_NAME_CODE_CONVERTER.toString(Controller.settings.getSetting(sLang)));
        Label lblLang = new Label(i18n.getString("settings.lang"));
        lblLang.setLabelFor(cbLang);

        CheckBox cbRememberFilePaths = new CheckBox(i18n.getString("settings.remember.file-path"));
        cbRememberFilePaths.setSelected(Controller.settings.getSettingBoolean(sRemember_Opened_Files));

        CheckBox cbReopenLastFile = new CheckBox(i18n.getString("settings.reopen-last-file"));
        cbReopenLastFile.setSelected(Controller.settings.getSettingBoolean(sReopen_Last_File));

        CheckBox cbUpdate = new CheckBox(i18n.getString("settings.update"));
        cbUpdate.setSelected(Controller.settings.getSettingBoolean(sCheck_For_Update_On_Startup));

        CheckBox cbConfirmDelete = new CheckBox(i18n.getString("settings.confirm-delete"));
        cbConfirmDelete.setSelected(Controller.settings.getSettingBoolean(sConfirm_DELETE));

        Button buttonSave = new Button(i18n.getString("button.save"));
        buttonSave.setOnAction(evt -> {
            // lang
            Controller.settings.setSetting(sLang, THE_LANG_NAME_CODE_CONVERTER.toString(cbLang.getSelectionModel().getSelectedItem()));

            // remember file path
            Controller.settings.setSetting(sRemember_Opened_Files, "" + cbRememberFilePaths.isSelected());

            // reopen last file
            Controller.settings.setSetting(sReopen_Last_File, "" + cbReopenLastFile.isSelected());

            // update
            Controller.settings.setSetting(sCheck_For_Update_On_Startup, "" + cbUpdate.isSelected());

            // confirm delete
            Controller.settings.setSetting(sConfirm_DELETE, "" + cbConfirmDelete.isSelected());

            // file path is set on io

            stage.hide();
        });
        buttonSave.setDefaultButton(true);

        Button buttonRestore = new Button(i18n.getString("button.restore-defaults"));
        buttonRestore.setOnAction(actionEvent -> {
            Controller.settings.restoreToDefault();
            stage.hide();
        });

        Button buttonCancel = new Button(i18n.getString("button.cancel"));
        buttonCancel.setOnAction(actionEvent -> stage.hide());
        buttonCancel.setCancelButton(true);

        HBox layoutLang = new HBox();
        layoutLang.setAlignment(Pos.BASELINE_CENTER);
        layoutLang.setSpacing(2);
        layoutLang.getChildren().addAll(lblLang, cbLang);

        HBox layoutButtons = new HBox();
        layoutButtons.setAlignment(Pos.BASELINE_CENTER);
        layoutButtons.setSpacing(5);
        layoutButtons.getChildren().addAll(buttonSave, buttonRestore, buttonCancel);

        TilePane layoutCheckBoxes = new TilePane(
                cbRememberFilePaths,
                cbReopenLastFile,
                cbUpdate,
                cbConfirmDelete
        );
        layoutCheckBoxes.setAlignment(Pos.BASELINE_LEFT);
        layoutCheckBoxes.setTileAlignment(Pos.BASELINE_LEFT);
        layoutCheckBoxes.setHgap(5);
        layoutCheckBoxes.setVgap(3);

        layout.setAlignment(Pos.CENTER_LEFT);
        layout.getChildren().addAll(
                layoutLang,
                lblCurrentFile, tfCurrentFile,
                layoutCheckBoxes,
                layoutButtons
        );
        layout.setSpacing(3);
        layout.setPrefSize(400, 300);

        buttonCancel.requestFocus();

        stage.setOnCloseRequest(windowEvent -> {
            windowEvent.consume();
            buttonCancel.fire();
        });

        stage.show();
    }
}
