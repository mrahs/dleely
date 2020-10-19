/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pw.ahs.app.dleely.Globals;
import pw.ahs.app.dleely.command.AddItemsCommand;
import pw.ahs.app.dleely.command.ICommand;
import pw.ahs.app.dleely.controller.Controller;
import pw.ahs.app.dleely.model.Item;
import pw.ahs.app.fxsimplecontrols.Dialogs;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static pw.ahs.app.dleely.Globals.*;
import static pw.ahs.app.dleely.controller.SettingsController.Setting.*;

public class App extends Application {

    /*
    ************************************************************
    *
    * UI Fields
    *
    ************************************************************/

    private Stage theStage;
    private final VBox root = new VBox();
    private final TextField searchBox = new TextField();
    private final MenuButton menuBtn = new MenuButton();
    private final ListView<Item> listView = new ListView<>();
    private final GridPane statusBar = new GridPane();

    private final Collection<Image> icons = new ArrayList<>(6);

    private final ObservableList<Item> items = FXCollections.observableArrayList();

     /*
    ************************************************************
    *
    * Logic Fields
    *
    ************************************************************/


    /*
    ************************************************************
    *
    * Methods
    *
    ************************************************************/

    public static void main(String[] args) {
        Application.launch(args);
    }

    private void initialize() {
        // icons
        int j = 8;
        for (int i = 0; i < 6; i++) {
            j *= 2;
            InputStream is = getClass().getResourceAsStream("/pw/ahs/app/dleely/res/dleely-icon-" + j + ".png");
            if (is == null) continue;
            icons.add(new Image(is));
        }
        Platform.runLater(() -> Splash.updateSplashIcons(icons));
        theStage.setTitle(APP_TITLE);
        theStage.getIcons().setAll(icons);

        // load globals & helpers & controllers
        try {
            Class.forName("Globals");
            Class.forName("UIHelper");
            Class.forName("Controller");
        } catch (ClassNotFoundException ignored) {
        }

        OneParamFunction<String> mainLangListener = (newLang) -> {
            if (newLang.equals("en")) {
                theCollator = COLLATOR_EN;
                i18n = ResourceBundle.getBundle("pw.ahs.app.dleely.i18n.word", Locale.US);
                theStage.getScene().setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            } else if (newLang.equals("ar")) {
                theCollator = COLLATOR_AR;
                i18n = ResourceBundle.getBundle("pw.ahs.app.dleely.i18n.word", LOCAL_AR);
                theStage.getScene().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            }
        };
        Controller.curLang.addListener(mainLangListener);

        Controller.unsaved.addListener(saved -> {
            if (saved) {
                theStage.setTitle(APP_TITLE);
            } else {
                theStage.setTitle(APP_TITLE + "*");
            }
        });

        // search box
        searchBox.setId("search-box");
        searchBox.setOnKeyReleased(evt -> {
            if (evt.getCode() == KeyCode.ENTER && evt.isControlDown()) {
                String text = searchBox.getText().trim();
                if (text.isEmpty()) return;
                searchBox.clear();

                Item item = new Item(i18n.getString("m.new"), text);
                ItemView itemView = new ItemView(item);
                itemView.setEditable(true);
                itemView.startEdit();
                if (Dialogs.showYesNoDialog(
                        view.getStage(),
                        i18n.getString("item.new.title"),
                        itemView,
                        i18n.getString("button.add"),
                        "",
                        i18n.getString("button.cancel"),
                        0
                ) == Dialogs.Result.CANCEL)
                    return;
                itemView.commitEdit();
                item = itemView.createItem();

                ICommand cmd = new AddItemsCommand(item);
                Controller.undoRedo.executeThenPushCommand(cmd);
            }
        });
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> UIHelper.io.search(newValue));
        // context menu bug
//        Platform.runLater(() -> searchBox.setContextMenu(new ContextMenu()));
//        searchBox.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, evt -> {
        // copy/cut/delete if text is selected
        // paste (at current caret position or replace selection) if clipboard has text
        // select all
//        });

        // controllers & helpers
        Controller.settings.init();
        UIHelper.menu.init(menuBtn, searchBox, listView);
        UIHelper.list.init(listView, searchBox);
//        UIHelper.settings.init(); // must be initialized the last one, for it will apply all options
        UIHelper.io.init();

        HBox subLayout = new HBox(searchBox, menuBtn);
        subLayout.setAlignment(Pos.CENTER);
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        VBox.setVgrow(listView, Priority.ALWAYS);
        root.getChildren().addAll(subLayout, listView, statusBar);
        root.setSpacing(2);
        root.setPrefSize(640, 480);

        // stage
        theStage.setOnCloseRequest(evt -> {
            evt.consume();
            exit();
        });

        // load settings
        if (!Controller.settings.load(PREFS_FILE_NAME))
            Dialogs.showMessageDialog(
                    theStage,
                    i18n.getString("settings.error.title"),
                    i18n.getString("settings.error.read-msg"),
                    i18n.getString("button.dismiss")
            );

        // default exception handler
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            String msg = getUserAgentString() + "\n" + stringWriter.toString();

            VBox vBox = new VBox();
            TextArea textArea = new TextArea(msg);
            Hyperlink hyperlink = new Hyperlink(i18n.getString("m.unknown-error.link"));
            hyperlink.setOnAction(evt -> {
                hyperlink.setOnAction(null);
                vBox.getChildren().addAll(textArea);
                hyperlink.getScene().getWindow().sizeToScene();
            });
            vBox.getChildren().addAll(new Label(i18n.getString("m.unknown-error.msg")), hyperlink);
            if (Dialogs.showYesNoDialog(
                    theStage,
                    i18n.getString("m.unknown-error.title"),
                    vBox,
                    i18n.getString("button.send"),
                    i18n.getString("button.discard"),
                    "",
                    1
            ) == Dialogs.Result.YES) {
                feedback(msg);
            }
        });

        finishInit();
    }

    private void finishInit() {
        Platform.runLater(() -> {
            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(getClass().getResource("/pw/ahs/app/dleely/gui/css/style.css").toExternalForm());
            scene.getStylesheets().addAll(getClass().getResource("/pw/ahs/app/dleely/gui/css/item-view-style.css").toExternalForm());
            theStage.setScene(scene);

            // show
            theStage.show();
            Splash.hide();

            // apply settings
            UIHelper.settings.init();

            // command line argument handler & last file
            List<String> params = getParameters().getUnnamed();
            if (params.isEmpty()) {
                if (Controller.settings.getSettingBoolean(sReopen_Last_File) &&
                        !Controller.settings.getSetting(sFile_Path).isEmpty()) {
                    UIHelper.io.open(Controller.settings.getSetting(sFile_Path).split(";")[0]);
                }
            } else {
                UIHelper.io.open(params.get(0));
            }

            // update
            if (Controller.settings.getSettingBoolean(sCheck_For_Update_On_Startup)) {
                checkForUpdate(false);
            }

            // focus search box
            searchBox.requestFocus();
        });
    }

    @Override
    public void start(Stage stage) throws Exception {
        Splash.show(APP_TITLE, new Image("/pw/ahs/app/dleely/res/dleely-icon-128.png"), icons);

        theStage = stage;
        Globals.view = this;
        new Thread(this::initialize).start();
    }

    public Stage getStage() {
        return theStage;
    }

    public ListView<Item> getListView() {
        return listView;
    }

    public ObservableList<Item> getItems() {
        return items;
    }

    public void exit() {
        if (waitForSave()) return;

        Controller.beforeExit.doAll();
        Platform.exit();
    }

    public boolean waitForSave() {
        if (Controller.unsaved.isSaved())
            return false;

        Dialogs.Result result = Dialogs.showYesNoDialog(
                theStage,
                i18n.getString("file.warning.title"),
                i18n.getString("file.warning.unsaved-change"),
                i18n.getString("button.save"),
                i18n.getString("button.discard"),
                i18n.getString("button.cancel"),
                3);
        if (result == Dialogs.Result.YES) {
            UIHelper.io.save(false);
        } else if (result == Dialogs.Result.CANCEL) {
            return true;
        }
        return false;
    }

    public void checkForUpdate(final boolean interactive) {
        String[] version = new String[]{""};
        Runnable processResult = () -> {
            if (version[0].isEmpty()) {
                if (interactive && Dialogs.showYesNoDialog(
                        theStage,
                        i18n.getString("update.title"),
                        i18n.getString("update.msg.error"),
                        i18n.getString("button.dismiss"),
                        i18n.getString("button.try-again"),
                        "",
                        2
                ) == Dialogs.Result.NO) {
                    checkForUpdate(true);
                }
            } else if (version[0].equalsIgnoreCase(APP_VERSION)) {
                if (interactive)
                    Dialogs.showMessageDialog(
                            theStage,
                            i18n.getString("update.title"),
                            i18n.getString("update.msg.no"),
                            i18n.getString("button.dismiss")
                    );
            } else {
                if (Dialogs.showYesNoDialog(
                        theStage,
                        i18n.getString("update.title"),
                        String.format(i18n.getString("update.msg.yes"), version[0]),
                        i18n.getString("button.dismiss"),
                        i18n.getString("button.update"),
                        "",
                        2
                ) == Dialogs.Result.NO) {
                    getHostServices().showDocument(Globals.APP_HOME);
                }
            }
        };

        if (interactive) {
            Stage stageWaiting = Dialogs.createWaitingDialog(
                    theStage,
                    i18n.getString("m.working"),
                    i18n.getString("update.working"),
                    i18n.getString("button.dismiss")
            );
            new Thread(() -> {
                version[0] = Controller.net.getLatestVersion();
                Platform.runLater(stageWaiting::hide);
            }).start();
            stageWaiting.showAndWait();
            processResult.run();
        } else
            new Thread(() -> {
                version[0] = Controller.net.getLatestVersion();
                Platform.runLater(processResult);
            }).start();
    }

    private boolean feedback(final String txt) {
        Stage stageWaiting = Dialogs.createWaitingDialog(
                theStage,
                i18n.getString("m.working"),
                i18n.getString("feedback.working"),
                i18n.getString("button.dismiss")
        );
        BooleanProperty state = new SimpleBooleanProperty(true);

        new Thread(() -> {
            state.set(Controller.net.sendFeedback(txt));
            Platform.runLater(stageWaiting::hide);
        }).start();
        stageWaiting.showAndWait();

        if (state.get()) {
            Dialogs.showMessageDialog(
                    theStage,
                    i18n.getString("feedback.title"),
                    i18n.getString("feedback.msg.success"),
                    i18n.getString("button.dismiss")
            );
        } else {
            if (Dialogs.showYesNoDialog(
                    theStage,
                    i18n.getString("feedback.title"),
                    i18n.getString("feedback.msg.fail"),
                    i18n.getString("button.dismiss"),
                    i18n.getString("button.try-again"),
                    "",
                    2
            ) == Dialogs.Result.NO) {
                return feedback(txt);
            }
        }
        searchBox.selectAll();
        return true;
    }
}
