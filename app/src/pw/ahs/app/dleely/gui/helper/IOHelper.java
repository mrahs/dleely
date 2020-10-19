/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.gui.helper;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import pw.ahs.app.dleely.command.ICommand;
import pw.ahs.app.dleely.command.ImportItemsCommand;
import pw.ahs.app.dleely.controller.Controller;
import pw.ahs.app.dleely.exporter.ExportConfigs;
import pw.ahs.app.dleely.gui.ItemView;
import pw.ahs.app.dleely.gui.UIHelper;
import pw.ahs.app.dleely.importer.ImportConfigs;
import pw.ahs.app.dleely.model.Item;
import pw.ahs.app.dleely.model.Tag;
import pw.ahs.app.dleely.store.H2DBStore;
import pw.ahs.app.fxsimplecontrols.Dialogs;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static pw.ahs.app.dleely.Globals.*;
import static pw.ahs.app.dleely.controller.SettingsController.Setting.sConfirm_DELETE;
import static pw.ahs.app.dleely.controller.SettingsController.Setting.sFile_Path;

public class IOHelper {
    private H2DBStore store = null;
    private static IOHelper instance = null;

    public static IOHelper getInstance() {
        if (instance == null)
            instance = new IOHelper();
        return instance;
    }

    private IOHelper() {
    }

    public void init() {
        Controller.beforeExit.addAction(() -> {
            try {
                _close();
            } catch (Exception e) {
                showError("file.error.title", "file.error.can-not-close");
            }
        });
        try {
            store = new H2DBStore(null);
            store.open(true, false);
        } catch (Exception e) {
            showError("db.error.title", "db.error.init");
        }
    }

    public boolean isFileOpen() {
        return store.getPath() != null;
    }

    public void newFile() {
        if (view.waitForSave()) return;

        FileChooser fc = new FileChooser();
        fc.setTitle(i18n.getString("chooser.save"));
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        fc.setInitialFileName(DEFAULT_FILE_NAME);
        fc.getExtensionFilters().add(DLEELY_EXT_FILTER);
        fc.setSelectedExtensionFilter(DLEELY_EXT_FILTER);
        File f = fc.showSaveDialog(view.getStage());
        if (f == null) return;

        try {
            _close();
        } catch (Exception e) {
            showError("file.error.title", "file.error.can-not-close");
            return;
        }

        try {
            store = new H2DBStore(f.toPath());
            if (store.checkInappropriateClose()) {
                Dialogs.Result result = Dialogs.showYesNoDialog(
                        view.getStage(),
                        i18n.getString("file.error.title"),
                        i18n.getString("file.error.crash"),
                        i18n.getString("button.recover"),
                        i18n.getString("button.ignore"),
                        i18n.getString("button.cancel"),
                        1
                );
                if (result == Dialogs.Result.CANCEL)
                    return;
                if (result == Dialogs.Result.YES)
                    store.open(false, true);
                else
                    store.open(true, false);
            } else {
                store.open(true, false);
            }
        } catch (Exception e) {
            showError("file.error.title", "file.error.can-not-create");
            return;
        }

        Controller.settings.setSetting(sFile_Path, Controller.util.handleRecentOpenedFiles(f.getAbsolutePath()));
        Controller.unsaved.setSaved(true);
    }

    private void showError(String title, String msg) {
        Dialogs.showMessageDialog(
                view.getStage(),
                i18n.getString(title),
                i18n.getString(msg),
                i18n.getString("button.dismiss")
        );
    }

    private Stage createWaitingStage(String msg) {
        return Dialogs.createWaitingDialog(
                view.getStage(),
                i18n.getString("m.working"),
                i18n.getString(msg),
                i18n.getString("button.dismiss")
        );
    }

    public void close() {
        if (view.waitForSave()) return;

        try {
            _close();
        } catch (Exception e) {
            showError("file.error.title", "file.error.can-not-close");
            return;
        }

        Controller.unsaved.setSaved(true);
        Controller.undoRedo.clearStacks();
    }

    private void _close() throws Exception {
        if (store == null) return;

        Stage waitingStage = createWaitingStage("file.working.close");
        ObjectProperty<Exception> e = new SimpleObjectProperty<>();
        BooleanProperty error = new SimpleBooleanProperty(false);
        new Thread(() -> {
            try {
                store.close();
            } catch (Exception ee) {
                error.set(true);
                e.set(ee);
            }
            Platform.runLater(waitingStage::hide);
        }).start();
        waitingStage.showAndWait();
        if (error.get()) throw e.get();

        store = new H2DBStore(null);
        store.open(true, false);
        view.getItems().clear();
    }

    public void open(String filePath) {
        if (view.waitForSave()) return;

        File f;
        if (filePath.isEmpty()) {
            FileChooser fc = new FileChooser();
            fc.setTitle(i18n.getString("chooser.open"));
            fc.setInitialDirectory(new File(System.getProperty("user.home")));
            fc.setInitialFileName(DEFAULT_FILE_NAME);
            fc.getExtensionFilters().add(DLEELY_EXT_FILTER);
            fc.setSelectedExtensionFilter(DLEELY_EXT_FILTER);
            f = fc.showOpenDialog(view.getStage());
            if (f == null) return;
        } else {
            f = new File(filePath);
        }

        try {
            _close();
        } catch (Exception e) {
            showError("file.error.title", "file.error.can-not-close");
            return;
        }

        try {
            store = new H2DBStore(f.toPath());
            if (store.checkInappropriateClose()) {
                Dialogs.Result result = Dialogs.showYesNoDialog(
                        view.getStage(),
                        i18n.getString("file.error.title"),
                        i18n.getString("file.error.crash"),
                        i18n.getString("button.recover"),
                        i18n.getString("button.ignore"),
                        i18n.getString("button.cancel"),
                        1
                );
                if (result == Dialogs.Result.CANCEL)
                    return;
                if (result == Dialogs.Result.YES)
                    store.open(false, true);
                else
                    store.open(false, false);
            } else {
                store.open(false, false);
            }
        } catch (Exception e) {
            showError("file.error.title", "file.error.can-not-open");
            return;
        }

        Stage waitingStage = createWaitingStage("file.working.load");
        BooleanProperty state = new SimpleBooleanProperty(true);
        ObservableList<Item> items = FXCollections.observableArrayList();
        items.addListener((ListChangeListener<Item>) c -> {
            while (c.next()) {
                if (c.wasAdded())
                    Platform.runLater(() -> {
                        view.getItems().addAll(c.getAddedSubList());
                    });
            }
        });
        new Thread(() -> {
            try {
                store.loadAllItems(items);
            } catch (Exception e) {
                state.set(false);
            }
            Platform.runLater(waitingStage::hide);
        }).start();
        waitingStage.showAndWait();

        if (state.get()) {
            Controller.settings.setSetting(sFile_Path, Controller.util.handleRecentOpenedFiles(f.getAbsolutePath()));
            Controller.unsaved.setSaved(true);
        } else {
            showError("file.error.title", "file.error.can-not-read");
        }
    }

    public void save(boolean newFile) {
        if ((store == null || store.getPath() == null) && view.getItems().isEmpty()) {
            // no file was opened and nothing to save
            return;
        }

        if (newFile || (store == null || store.getPath() == null))
            _saveAs();
//        else
//            _save();
    }

    private void _saveAs() {
        FileChooser fc = new FileChooser();
        fc.setTitle(i18n.getString("chooser.save"));
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        fc.setInitialFileName(DEFAULT_FILE_NAME);
        fc.getExtensionFilters().add(DLEELY_EXT_FILTER);
        fc.setSelectedExtensionFilter(DLEELY_EXT_FILTER);
        File f = fc.showSaveDialog(view.getStage());
        if (f == null) return; // not an error, the user just canceled

        final H2DBStore[] tmpStore = new H2DBStore[1];
        try {
            tmpStore[0] = new H2DBStore(f.toPath());
            if (tmpStore[0].checkInappropriateClose()) {
                Dialogs.Result result = Dialogs.showYesNoDialog(
                        view.getStage(),
                        i18n.getString("file.error.title"),
                        i18n.getString("file.error.crash"),
                        i18n.getString("button.recover"),
                        i18n.getString("button.ignore"),
                        i18n.getString("button.cancel"),
                        1
                );
                if (result == Dialogs.Result.CANCEL)
                    return;
                if (result == Dialogs.Result.YES)
                    tmpStore[0].open(false, true);
                else
                    tmpStore[0].open(true, false);
            } else {
                tmpStore[0].open(true, false);
            }
        } catch (Exception e) {
            showError("file.error.title", "file.error.can-not-create");
            return;
        }

        final Stage waiting = createWaitingStage("file.working.save");
        final BooleanProperty error = new SimpleBooleanProperty(false);
        new Thread(() -> {
            try {
                tmpStore[0].addUpdateItems(store.getAllItems(), false);
            } catch (Exception e) {
                error.set(false);
                if (tmpStore[0] != null) {
                    try {
                        tmpStore[0].close();
                    } catch (Exception ignored) {
                    }
                }
            }

            if (!error.get()) {
                try {
                    store.close();
                    store = tmpStore[0];
                } catch (Exception e) {
                    error.set(false);
                }
            }
            Platform.runLater(waiting::hide);
        }).start();

        waiting.showAndWait();

        if (error.get()) {
            showError("file.error.title", "file.error.can-not-save");
            Controller.unsaved.setSaved(false); // just in case!
        } else {
            Controller.unsaved.setSaved(true);
            Controller.settings.setSetting(sFile_Path, Controller.util.handleRecentOpenedFiles(f.getAbsolutePath()));
//            Controller.settings.setSetting(sFile_Path, f.getAbsolutePath());
        }
    }

    public boolean addItem(Item item) {
        try {
            if (store.itemExist(item.getRef(), null)) {
                Dialogs.showMessageDialog(
                        view.getStage(),
                        i18n.getString("item.error.already.title"),
                        i18n.getString("item.error.already.msg").replace("%date", item.getDateAddText()),
                        i18n.getString("button.select")
                );
                view.getListView().getSelectionModel().clearSelection();
                view.getListView().getSelectionModel().select(item);
                view.getListView().scrollTo(view.getListView().getSelectionModel().getSelectedIndex());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        try {
            store.addUpdateItem(item, false);
        } catch (Exception e) {
            showError("db.error.title", "db.error.insert");
            return false;
        }
        view.getListView().getSelectionModel().clearSelection();
        view.getItems().add(item);
        view.getListView().getSelectionModel().select(item);
        view.getListView().scrollTo(view.getListView().getSelectionModel().getSelectedIndex());
        // Some events will be triggered after returning from this method
        // which will cause the cell to update and cancel editing.
        // That's why we are scheduling the edit later, to run after those events.
        // The selection and scrolling must be done before scheduling.
//        Platform.runLater(() -> view.getListView().edit(view.getListView().getSelectionModel().getSelectedIndex()));
        if (Controller.unsaved.isSaved() && !isFileOpen())
            Controller.unsaved.setSaved(false);
        return true;
    }

    public boolean addItems(Collection<Item> items) {
        Stage waiting = Dialogs.createWaitingDialog(view.getStage(), i18n.getString("item.add.title"), i18n.getString("item.add.working"), i18n.getString("button.dismiss"));
        BooleanProperty error = new SimpleBooleanProperty(false);
        new Thread(() -> {
            try {
                for (Item item : items) {
                    if (store.addUpdateItem(item, false))
                        Platform.runLater(() -> view.getItems().add(item));
                }
            } catch (Exception e) {
                error.set(true);
            }
            Platform.runLater(waiting::hide);
        }).start();
        waiting.showAndWait();

        if (error.get()) {
            showError("item.error.add.title", "item.error.add.msg");
            return false;
        } else {
            return true;
        }
    }

    public boolean editItem(Item oldValue, Item newValue) {
        try {
            store.addUpdateItem(newValue, true);
            int i = view.getItems().indexOf(oldValue);
            view.getItems().set(i, newValue);
            view.getListView().getSelectionModel().clearAndSelect(i);
            view.getListView().scrollTo(i);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean editItems(Collection<Item[]> pairs) {
        Stage waiting = Dialogs.createWaitingDialog(view.getStage(), i18n.getString("item.edit.title"), i18n.getString("item.edit.working"), i18n.getString("button.dismiss"));
        BooleanProperty error = new SimpleBooleanProperty(false);
        new Thread(() -> {
            try {
                for (Item[] pair : pairs) {
                    store.addUpdateItem(pair[1], true);
                    Platform.runLater(() -> {
                        int i = view.getItems().indexOf(pair[0]);
                        view.getItems().set(i, pair[1]);
                    });
                }
            } catch (Exception e) {
                error.set(true);
            }
            Platform.runLater(waiting::hide);
        }).start();
        waiting.showAndWait();

        if (error.get()) {
            showError("item.error.edit.title", "item.error.edit.msg");
            return false;
        } else {
            return true;
        }
    }

    public boolean deleteItems(Collection<Item> items) {
        if (items.isEmpty()) return false;

        if (Controller.settings.getSettingBoolean(sConfirm_DELETE))
            if (Dialogs.showYesNoDialog(
                    view.getStage(),
                    i18n.getString("m.delete.title"),
                    i18n.getString("m.delete.msg").replace("%d", "" + items.size()),
                    i18n.getString("button.delete"),
                    i18n.getString("button.cancel"),
                    "",
                    2
            ) != Dialogs.Result.YES)
                return false;

        long[] ids = new long[items.size()];
        Iterator<Item> itr = items.iterator();
        for (int i = 0; i < items.size(); i++) {
            ids[i] = itr.next().getId();
        }
        try {
            if (!store.removeItems(ids)) {
                showError("item.error.del.title", "item.error.del.msg");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        view.getItems().removeAll(items);
        return true;
    }

    public long importItems(boolean useClipboard) {
        StringConverter<Tag[]> textTag = new StringConverter<Tag[]>() {
            @Override
            public String toString(Tag[] object) {
                return Controller.util.joinArray(object, " ");
            }

            @Override
            public Tag[] fromString(String string) {
                String[] tagsTextArray = string.split("\\s");
                ArrayList<Tag> tmp = new ArrayList<>(tagsTextArray.length);
                for (String tagText : tagsTextArray) {
                    if (!Tag.isValidTagName(tagText)) continue;
                    tmp.add(Tag.getInstance(tagText));
                }

                return tmp.toArray(new Tag[tmp.size()]);
            }
        };
        long[] count = new long[1];

        // options
        // name
        TextField tfDefaultName = new TextField();
        Label lblDefaultName = new Label();
        lblDefaultName.setLabelFor(tfDefaultName);
        // info
        TextArea taDefaultInfo = new TextArea();
        Label lblDefaultInfo = new Label();
        lblDefaultInfo.setLabelFor(taDefaultInfo);
        // privacy
        ChoiceBox<Boolean> cbDefaultPrivacy = new ChoiceBox<>();
        Label lblDefaultPrivacy = new Label();
        lblDefaultPrivacy.setLabelFor(cbDefaultPrivacy);
        // tags
        TextArea taDefaultTags = new TextArea();
        Label lblDefaultTags = new Label();
        lblDefaultTags.setLabelFor(taDefaultTags);

        // collision options
        ArrayList<ImportConfigs.CollisionOption> collisionOptions =
                new ArrayList<>(Arrays.asList(ImportConfigs.CollisionOption.values()));
        ArrayList<ImportConfigs.CollisionOption> collisionOptions2 =
                new ArrayList<>(Arrays.asList(ImportConfigs.CollisionOption.SKIP, ImportConfigs.CollisionOption.REPLACE));
        // skip whole
        ToggleButton tbSkipWhole = new ToggleButton();
        // id is always skipped
        // name
        ChoiceBox<ImportConfigs.CollisionOption> cbName =
                new ChoiceBox<>(FXCollections.observableArrayList(collisionOptions));
        Label lblName = new Label();
        lblName.setLabelFor(cbName);
        // info
        ChoiceBox<ImportConfigs.CollisionOption> cbInfo =
                new ChoiceBox<>(FXCollections.observableArrayList(collisionOptions));
        Label lblInfo = new Label();
        lblInfo.setLabelFor(cbInfo);
        // privy
        ChoiceBox<ImportConfigs.CollisionOption> cbPrivy =
                new ChoiceBox<>(FXCollections.observableArrayList(collisionOptions2));
        Label lblPrivy = new Label();
        lblPrivy.setLabelFor(cbPrivy);
        // tags
        ChoiceBox<ImportConfigs.CollisionOption> cbTags =
                new ChoiceBox<>(FXCollections.observableArrayList(collisionOptions));
        Label lblTags = new Label();
        lblTags.setLabelFor(cbTags);
        // date added
        ChoiceBox<ImportConfigs.CollisionOption> cbDateAdd =
                new ChoiceBox<>(FXCollections.observableArrayList(collisionOptions2));
        Label lblDateAdd = new Label();
        lblDateAdd.setLabelFor(cbDateAdd);
        // date modified
        ChoiceBox<ImportConfigs.CollisionOption> cbDateMod =
                new ChoiceBox<>(FXCollections.observableArrayList(collisionOptions2));
        Label lblDateMod = new Label();
        lblDateMod.setLabelFor(cbDateAdd);
        // collision tags
        TextArea taCollisionTags = new TextArea();
        Label lblCollisionTags = new Label();
        lblCollisionTags.setLabelFor(taCollisionTags);

        // file
        RadioButton rbFile = new RadioButton();
        RadioButton rbClipboard = new RadioButton();
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(rbFile, rbClipboard);
        TextField tfFile = new TextField();
        Button btnFile = new Button();
        ChoiceBox<FileFormat> cbFile = new ChoiceBox<>();

        // buttons
        Button btnImport = new Button();
        Button btnCancel = new Button();
        Button btnRestore = new Button();

        // language
        Label lblOptions = new Label(i18n.getString("import.options"));
        Label lblCollisionOptions = new Label(i18n.getString("import.collision-options"));
        lblDefaultName.setText(i18n.getString("import.default.name"));
        lblDefaultInfo.setText(i18n.getString("import.default.info"));
        lblDefaultPrivacy.setText(i18n.getString("import.default.privy"));
        cbDefaultPrivacy.getItems().addAll(Boolean.FALSE, Boolean.TRUE);
        cbDefaultPrivacy.setConverter(new StringConverter<Boolean>() {
            @Override
            public String toString(Boolean object) {
                if (object) return i18n.getString("import.default.privy.private");
                else return i18n.getString("import.default.privy.public");
            }

            @Override
            public Boolean fromString(String string) {
                return string.equals(i18n.getString("import.default.privy.private"));
            }
        });
        lblDefaultTags.setText(i18n.getString("import.default-tags"));
        StringConverter<ImportConfigs.CollisionOption> stringConverterCollisionOption =
                new StringConverter<ImportConfigs.CollisionOption>() {
                    @Override
                    public String toString(ImportConfigs.CollisionOption object) {
                        if (object == ImportConfigs.CollisionOption.SKIP)
                            return i18n.getString("import.skip");
                        else if (object == ImportConfigs.CollisionOption.REPLACE)
                            return i18n.getString("import.replace");
                        else
                            return i18n.getString("import.merge");
                    }

                    @Override
                    public ImportConfigs.CollisionOption fromString(String string) {
                        if (string.equals(i18n.getString("import.skip")))
                            return ImportConfigs.CollisionOption.SKIP;
                        else if (string.equals(i18n.getString("import.replace")))
                            return ImportConfigs.CollisionOption.REPLACE;
                        else
                            return ImportConfigs.CollisionOption.MERGE;
                    }
                };
        lblTags.setText(i18n.getString("import.tags"));
        cbTags.setConverter(stringConverterCollisionOption);
        tbSkipWhole.setText(i18n.getString("import.skip-whole"));
        lblName.setText(i18n.getString("import.name"));
        cbName.setConverter(stringConverterCollisionOption);
        lblInfo.setText(i18n.getString("import.info"));
        cbInfo.setConverter(stringConverterCollisionOption);
        lblPrivy.setText(i18n.getString("import.privy"));
        cbPrivy.setConverter(stringConverterCollisionOption);
        lblCollisionTags.setText(i18n.getString("import.collision-tags"));
        lblDateAdd.setText(i18n.getString("import.date-add"));
        cbDateAdd.setConverter(stringConverterCollisionOption);
        lblDateMod.setText(i18n.getString("import.date-mod"));
        cbDateMod.setConverter(stringConverterCollisionOption);

        rbFile.setText(i18n.getString("import.file"));
        rbClipboard.setText(i18n.getString("import.cp"));
        btnFile.setText(i18n.getString("button.browse"));
        cbFile.setConverter(new StringConverter<FileFormat>() {
            @Override
            public String toString(FileFormat object) {
                switch (object) {
                    case DIIGO:
                        return "Diigo";
                    case DLEELY_CVS:
                        return "Dleely CSV";
                    case DLEELY_JSON:
                        return "Dleely JSON";
                    case DLEELY_XML:
                        return "Dleely XML";
                    case NETSCAPE:
                        return "Netscape";
                    default:
                        return "";
                }
            }

            @Override
            public FileFormat fromString(String string) {
                switch (string) {
                    case "Diigo":
                        return FileFormat.DIIGO;
                    case "Dleely CSV":
                        return FileFormat.DLEELY_CVS;
                    case "Dleely XML":
                        return FileFormat.DLEELY_XML;
                    case "Dleely JSON":
                        return FileFormat.DLEELY_JSON;
                    case "Netscape":
                        return FileFormat.NETSCAPE;
                    default:
                        return null;
                }
            }
        });
        cbFile.getItems().addAll(FileFormat.values());

        btnImport.setText(i18n.getString("button.import"));
        btnCancel.setText(i18n.getString("button.cancel"));
        btnRestore.setText(i18n.getString("button.restore-defaults"));

        // layout
        GridPane layout = new GridPane();
        layout.add(lblOptions, 0, 0, 2, 1);
        layout.add(lblDefaultName, 0, 1);
        layout.add(tfDefaultName, 1, 1);
        layout.add(lblDefaultPrivacy, 0, 2);
        layout.add(cbDefaultPrivacy, 1, 2);
        layout.add(lblDefaultInfo, 0, 3);
        layout.add(taDefaultInfo, 0, 4, 2, 1);
        layout.add(lblDefaultTags, 0, 5);
        layout.add(taDefaultTags, 0, 6, 2, 1);

        layout.add(lblCollisionOptions, 0, 7, 2, 1);
        layout.add(tbSkipWhole, 0, 8, 2, 1);

        layout.add(lblName, 0, 9);
        layout.add(cbName, 1, 9);
        layout.add(lblInfo, 0, 10);
        layout.add(cbInfo, 1, 10);
        layout.add(lblTags, 0, 11);
        layout.add(cbTags, 1, 11);
        layout.add(lblPrivy, 0, 12);
        layout.add(cbPrivy, 1, 12);
        layout.add(lblDateAdd, 0, 13);
        layout.add(cbDateAdd, 1, 13);
        layout.add(lblDateMod, 0, 14);
        layout.add(cbDateMod, 1, 14);
        layout.add(lblCollisionTags, 0, 15);
        layout.add(taCollisionTags, 0, 16, 2, 1);

        HBox layoutFile = new HBox(cbFile, tfFile, btnFile);
        HBox.setHgrow(tfFile, Priority.ALWAYS);
        HBox layoutSrc = new HBox(rbFile, rbClipboard);
        layoutSrc.setSpacing(5);
        layout.add(layoutSrc, 0, 17, 2, 1);
        layout.add(layoutFile, 0, 18, 2, 1);

        HBox layoutButtons = new HBox(btnImport, btnRestore, btnCancel);
        layoutButtons.setSpacing(3);
        layoutButtons.setAlignment(Pos.CENTER);
        layout.add(layoutButtons, 0, 19, 2, 1);
//        layout.setGridLinesVisible(true);

        // setup
        ImportConfigs importConfigs = new ImportConfigs();
        tfFile.disableProperty().bind(rbClipboard.selectedProperty());
        btnFile.disableProperty().bind(rbClipboard.selectedProperty());
        Runnable setDefaults = () -> {
            toggleGroup.selectToggle(useClipboard ? rbClipboard : rbFile);
            cbFile.getSelectionModel().selectFirst();
            tfDefaultName.setText(i18n.getString("import.default.name.unnamed"));
            taDefaultInfo.clear();
            taDefaultTags.clear();
            cbDefaultPrivacy.getSelectionModel().select(importConfigs.getDefaultPrivy());
            tbSkipWhole.setSelected(importConfigs.isSkipWhole());
            cbName.getSelectionModel().select(importConfigs.getCollisionOption(Item.Field.NAME));
            cbInfo.getSelectionModel().select(importConfigs.getCollisionOption(Item.Field.INFO));
            cbTags.getSelectionModel().select(importConfigs.getCollisionOption(Item.Field.TAGS));
            cbPrivy.getSelectionModel().select(importConfigs.getCollisionOption(Item.Field.PRIVY));
            cbDateAdd.getSelectionModel().select(importConfigs.getCollisionOption(Item.Field.DATE_ADD));
            cbDateMod.getSelectionModel().select(importConfigs.getCollisionOption(Item.Field.DATE_MODIFY));
            taCollisionTags.clear();
        };
        setDefaults.run();

        taDefaultInfo.setPrefRowCount(2);
        taDefaultTags.setPrefRowCount(2);
        taCollisionTags.setPrefRowCount(2);
        tbSkipWhole.setMaxWidth(Double.MAX_VALUE);
        tbSkipWhole.setTextAlignment(TextAlignment.CENTER);

        lblOptions.setMaxWidth(Double.MAX_VALUE);
        lblCollisionOptions.setMaxWidth(Double.MAX_VALUE);
        lblOptions.setAlignment(Pos.BASELINE_CENTER);
        lblCollisionOptions.setAlignment(Pos.BASELINE_CENTER);
        layoutSrc.setAlignment(Pos.BASELINE_CENTER);

        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setHgap(5);
        layout.setVgap(5);
        layout.setPadding(new Insets(5));

        lblName.disableProperty().bind(tbSkipWhole.selectedProperty());
        lblInfo.disableProperty().bind(tbSkipWhole.selectedProperty());
        lblPrivy.disableProperty().bind(tbSkipWhole.selectedProperty());
        lblTags.disableProperty().bind(tbSkipWhole.selectedProperty());
        lblDateAdd.disableProperty().bind(tbSkipWhole.selectedProperty());
        lblDateMod.disableProperty().bind(tbSkipWhole.selectedProperty());
        lblCollisionTags.disableProperty().bind(tbSkipWhole.selectedProperty());
        cbName.disableProperty().bind(tbSkipWhole.selectedProperty());
        cbInfo.disableProperty().bind(tbSkipWhole.selectedProperty());
        cbPrivy.disableProperty().bind(tbSkipWhole.selectedProperty());
        cbTags.disableProperty().bind(tbSkipWhole.selectedProperty());
        cbDateAdd.disableProperty().bind(tbSkipWhole.selectedProperty());
        cbDateMod.disableProperty().bind(tbSkipWhole.selectedProperty());
        taCollisionTags.disableProperty().bind(tbSkipWhole.selectedProperty());

        btnFile.setOnAction(evt -> {
            FileChooser fc = new FileChooser();
            fc.setTitle(i18n.getString("import.chooser.title"));
            fc.setInitialDirectory(new File(System.getProperty("user.home")));
            fc.getExtensionFilters().add(IMPORT_EXT_FILTER);
            fc.setSelectedExtensionFilter(IMPORT_EXT_FILTER);
            File f = fc.showOpenDialog(view.getStage());
            if (f == null) return;
            tfFile.setText(f.getAbsolutePath());
        });

        UIHelper.hackTextAreaTab(taDefaultInfo);
        UIHelper.hackTextAreaTab(taDefaultTags);
        UIHelper.hackTextAreaTab(taCollisionTags);

        // stage
        Stage stage = Dialogs.createUtilityDialog(view.getStage(), i18n.getString("import.title"), layout);

        // buttons
        btnImport.setDefaultButton(true);
        btnCancel.setCancelButton(true);

        btnImport.setOnAction(evt -> {
            final Path fPath;
            final StringReader reader;

            if (rbClipboard.isSelected()) {
                if (!Clipboard.getSystemClipboard().hasContent(DataFormat.PLAIN_TEXT)) {
                    showError("import.error.title", "import.error.cp.msg");
                    return;
                }
                fPath = null;
                reader = new StringReader(Clipboard.getSystemClipboard().getContent(DataFormat.PLAIN_TEXT).toString());
            } else if (tfFile.getText().isEmpty()) {
                showError("file.error.title", "file.error.invalid-file");
                return;
            } else {
                try {
                    fPath = Paths.get(tfFile.getText());
                    reader = null;
                } catch (InvalidPathException e) {
                    showError("file.error.title", "file.error.invalid-file");
                    return;
                }
            }

            // build configs
            importConfigs.setDefaultName(tfDefaultName.getText());
            importConfigs.setDefaultInfo(taDefaultInfo.getText());
            importConfigs.setDefaultPrivy(cbDefaultPrivacy.getValue());
            importConfigs.addTags(textTag.fromString(taDefaultTags.getText()));
            importConfigs.setSkipWhole(tbSkipWhole.isSelected());
            importConfigs.setCollisionOption(Item.Field.NAME, cbName.getValue());
            importConfigs.setCollisionOption(Item.Field.INFO, cbInfo.getValue());
            importConfigs.setCollisionOption(Item.Field.PRIVY, cbPrivy.getValue());
            importConfigs.setCollisionOption(Item.Field.TAGS, cbTags.getValue());
            importConfigs.setCollisionOption(Item.Field.DATE_ADD, cbDateAdd.getValue());
            importConfigs.setCollisionOption(Item.Field.DATE_MODIFY, cbDateMod.getValue());
            importConfigs.addCollisionTags(textTag.fromString(taCollisionTags.getText()));

            // prepare undo
            Collection<Item> added;
            Collection<Item[]> edited;
            ICommand cmd;
            added = new HashSet<>();
            edited = new HashSet<>();
            cmd = new ImportItemsCommand(added, edited);

            // call import
            BooleanProperty error = new SimpleBooleanProperty(false);
            Stage waitingStage = createWaitingStage("file.working.import");
            new Thread(() -> {
                try {
                    if (reader != null)
                        count[0] = store.importItems(reader, cbFile.getValue(), importConfigs, null,
                                item -> {
                                    Platform.runLater(() -> {
                                        view.getItems().remove(item);
                                        view.getItems().add(item);
                                    });
                                    added.add(item);
                                },
                                (oldItem, newItem) -> {
                                    Platform.runLater(() -> {
                                        view.getItems().remove(oldItem);
                                        view.getItems().add(newItem);
                                    });
                                    edited.add(new Item[]{oldItem, newItem});
                                }
                        );
                    else
                        count[0] = store.importItems(fPath, cbFile.getValue(), importConfigs, null,
                                item -> {
                                    Platform.runLater(() -> {
                                        view.getItems().remove(item);
                                        view.getItems().add(item);
                                    });
                                    added.add(item);
                                },
                                (oldItem, newItem) -> {
                                    Platform.runLater(() -> {
                                        view.getItems().remove(oldItem);
                                        view.getItems().add(newItem);
                                    });
                                    edited.add(new Item[]{oldItem, newItem});
                                }
                        );
                } catch (Exception e) {
                    error.set(true);
                }
                Platform.runLater(waitingStage::hide);
            }).start();
            waitingStage.showAndWait();
            if (error.get()) {
                showError("file.error.title", "file.error.import");
            } else {
                if (Controller.unsaved.isSaved() && !isFileOpen())
                    Controller.unsaved.setSaved(false);
                Controller.undoRedo.pushCommand(cmd);
            }

            stage.hide();
        });
        btnCancel.setOnAction(evt -> stage.hide());
        btnRestore.setOnAction(evt -> setDefaults.run());

        // show
        stage.setResizable(false);
        btnFile.requestFocus();
        stage.showAndWait();
        return count[0];
    }

    public void exportItems(boolean useClipboard) {
        // options
        Label lblOptions = new Label();
        ChoiceBox<Boolean> cbExportSelected = new ChoiceBox<>(FXCollections.observableArrayList(true, false));
        CheckBox cbSkipId = new CheckBox();
        CheckBox cbSkipName = new CheckBox();
        CheckBox cbSkipRef = new CheckBox();
        CheckBox cbSkipInfo = new CheckBox();
        CheckBox cbSkipPrivy = new CheckBox();
        CheckBox cbSkipTags = new CheckBox();
        CheckBox cbSkipDateAdd = new CheckBox();
        CheckBox cbSkipDateMod = new CheckBox();

        // file
        RadioButton rbFile = new RadioButton();
        RadioButton rbClipboard = new RadioButton();
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(rbFile, rbClipboard);
        TextField tfFile = new TextField();
        Button btnFile = new Button();
        ChoiceBox<FileFormat> cbFile = new ChoiceBox<>();

        // buttons
        Button btnExport = new Button();
        Button btnCancel = new Button();
        Button btnRestore = new Button();

        // language
        lblOptions.setText(i18n.getString("export.options"));
        cbExportSelected.setConverter(new StringConverter<Boolean>() {
            @Override
            public String toString(Boolean object) {
                return object ? i18n.getString("export.selected") : i18n.getString("export.all");
            }

            @Override
            public Boolean fromString(String string) {
                return string.equals(i18n.getString("export.selected"));
            }
        });
        cbSkipId.setText(i18n.getString("export.skip.id"));
        cbSkipName.setText(i18n.getString("export.skip.name"));
        cbSkipRef.setText(i18n.getString("export.skip.ref"));
        cbSkipInfo.setText(i18n.getString("export.skip.info"));
        cbSkipPrivy.setText(i18n.getString("export.skip.privy"));
        cbSkipTags.setText(i18n.getString("export.skip.tags"));
        cbSkipDateAdd.setText(i18n.getString("export.skip.date-add"));
        cbSkipDateMod.setText(i18n.getString("export.skip.date-mod"));
        rbFile.setText(i18n.getString("export.file"));
        rbClipboard.setText(i18n.getString("export.cp"));
        btnFile.setText(i18n.getString("button.browse"));
        cbFile.setConverter(new StringConverter<FileFormat>() {
            @Override
            public String toString(FileFormat object) {
                switch (object) {
                    case DIIGO:
                        return "Diigo";
                    case DLEELY_CVS:
                        return "Dleely CSV";
                    case DLEELY_JSON:
                        return "Dleely JSON";
                    case DLEELY_XML:
                        return "Dleely XML";
                    case NETSCAPE:
                        return "Netscape";
                    default:
                        return "";
                }
            }

            @Override
            public FileFormat fromString(String string) {
                switch (string) {
                    case "Diigo":
                        return FileFormat.DIIGO;
                    case "Dleely CSV":
                        return FileFormat.DLEELY_CVS;
                    case "Dleely XML":
                        return FileFormat.DLEELY_XML;
                    case "Dleely JSON":
                        return FileFormat.DLEELY_JSON;
                    case "Netscape":
                        return FileFormat.NETSCAPE;
                    default:
                        return null;
                }
            }
        });
        cbFile.getItems().addAll(FileFormat.values());

        btnExport.setText(i18n.getString("button.export"));
        btnCancel.setText(i18n.getString("button.cancel"));
        btnRestore.setText(i18n.getString("button.restore-defaults"));

        // layout
        TilePane subLayout = new TilePane(
                cbExportSelected,
                cbSkipId, cbSkipName, cbSkipRef,
                cbSkipInfo, cbSkipTags, cbSkipPrivy,
                cbSkipDateAdd, cbSkipDateMod
        );

        HBox layoutButtons = new HBox(
                btnExport, btnRestore, btnCancel
        );
        HBox layoutFile = new HBox(
                cbFile, tfFile, btnFile
        );
        HBox layoutDest = new HBox(rbFile, rbClipboard);

        VBox layout = new VBox(
                lblOptions,
                subLayout,
                layoutDest,
                layoutFile,
                layoutButtons
        );

        // setup
        layout.setSpacing(5);
        layoutButtons.setSpacing(3);
        layoutDest.setSpacing(5);
        subLayout.setHgap(5);
        subLayout.setVgap(5);
        subLayout.setPrefColumns(3);

        lblOptions.setAlignment(Pos.CENTER);
        layoutDest.setAlignment(Pos.CENTER);
        subLayout.setAlignment(Pos.CENTER_LEFT);
        subLayout.setOrientation(Orientation.HORIZONTAL);
        subLayout.setAlignment(Pos.CENTER_LEFT);
        subLayout.setTileAlignment(Pos.CENTER_LEFT);
        layoutButtons.setAlignment(Pos.CENTER);
        layoutFile.setAlignment(Pos.CENTER);
        layout.setAlignment(Pos.CENTER_LEFT);

        lblOptions.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(tfFile, Priority.ALWAYS);

        btnFile.setOnAction(evt -> {
            FileChooser fc = new FileChooser();
            fc.setTitle(i18n.getString("export.chooser.title"));
            fc.setInitialDirectory(new File(System.getProperty("user.home")));
            fc.getExtensionFilters().add(IMPORT_EXT_FILTER);
            fc.setSelectedExtensionFilter(IMPORT_EXT_FILTER);
            File f = fc.showSaveDialog(view.getStage());
            if (f == null) return;
            tfFile.setText(Controller.io.setFileExt(f.toPath(), cbFile.getValue().getExt()).toString());
        });

        tfFile.disableProperty().bind(rbClipboard.selectedProperty());
        btnFile.disableProperty().bind(rbClipboard.selectedProperty());

        Runnable setDefaults = () -> {
            cbFile.setValue(FileFormat.DLEELY_CVS);
            cbExportSelected.setValue(useClipboard);
            toggleGroup.selectToggle(useClipboard ? rbClipboard : rbFile);
        };
        cbFile.valueProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case DIIGO:
                    cbSkipId.setSelected(true);
                    cbSkipId.setDisable(true);

                    cbSkipName.setSelected(false);
                    cbSkipName.setDisable(false);

                    cbSkipRef.setSelected(false);
                    cbSkipRef.setDisable(false);

                    cbSkipInfo.setSelected(false);
                    cbSkipInfo.setDisable(false);

                    cbSkipPrivy.setSelected(true);
                    cbSkipPrivy.setDisable(true);

                    cbSkipTags.setSelected(false);
                    cbSkipTags.setDisable(false);

                    cbSkipDateAdd.setSelected(true);
                    cbSkipDateAdd.setDisable(true);

                    cbSkipDateMod.setSelected(true);
                    cbSkipDateMod.setDisable(true);
                    break;
                case NETSCAPE:
                    cbSkipId.setSelected(true);
                    cbSkipId.setDisable(true);

                    cbSkipName.setSelected(false);
                    cbSkipName.setDisable(false);

                    cbSkipRef.setSelected(false);
                    cbSkipRef.setDisable(false);

                    cbSkipInfo.setSelected(false);
                    cbSkipInfo.setDisable(false);

                    cbSkipPrivy.setSelected(false);
                    cbSkipPrivy.setDisable(false);

                    cbSkipTags.setSelected(false);
                    cbSkipTags.setDisable(false);

                    cbSkipDateAdd.setSelected(false);
                    cbSkipDateAdd.setDisable(false);

                    cbSkipDateMod.setSelected(false);
                    cbSkipDateMod.setDisable(false);
                    break;
                case DLEELY_CVS:
                case DLEELY_JSON:
                case DLEELY_XML:
                    cbSkipId.setSelected(true);
                    cbSkipId.setDisable(false);

                    cbSkipName.setSelected(false);
                    cbSkipName.setDisable(false);

                    cbSkipRef.setSelected(false);
                    cbSkipRef.setDisable(false);

                    cbSkipInfo.setSelected(false);
                    cbSkipInfo.setDisable(false);

                    cbSkipPrivy.setSelected(false);
                    cbSkipPrivy.setDisable(false);

                    cbSkipTags.setSelected(false);
                    cbSkipTags.setDisable(false);

                    cbSkipDateAdd.setSelected(false);
                    cbSkipDateAdd.setDisable(false);

                    cbSkipDateMod.setSelected(false);
                    cbSkipDateMod.setDisable(false);
                    break;
            }
        });
        setDefaults.run();

        // stage
        Stage stage = Dialogs.createUtilityDialog(view.getStage(), i18n.getString("export.title"), layout);

        // buttons
        btnExport.setDefaultButton(true);
        btnCancel.setCancelButton(true);

        btnCancel.setOnAction(evt -> stage.hide());
        btnRestore.setOnAction(evt -> setDefaults.run());

        btnExport.setOnAction(evt -> {
            if (cbExportSelected.getValue() && view.getListView().getSelectionModel().isEmpty()) {
                stage.hide();
                return;
            }

            final Path path;
            final StringWriter writer;
            if (rbFile.isSelected()) {
                if (tfFile.getText().isEmpty()) {
                    showError("file.error.title", "file.error.invalid-file");
                    return;
                } else {
                    try {
                        path = Paths.get(tfFile.getText());
                        writer = null;
                    } catch (InvalidPathException e) {
                        showError("file.error.title", "file.error.invalid-file");
                        return;
                    }
                }
            } else {
                path = null;
                writer = new StringWriter();
            }

            ExportConfigs exportConfigs = new ExportConfigs();
            if (cbSkipId.isSelected())
                exportConfigs.addSkipField(Item.Field.ID);
            if (cbSkipName.isSelected())
                exportConfigs.addSkipField(Item.Field.NAME);
            if (cbSkipRef.isSelected())
                exportConfigs.addSkipField(Item.Field.REF);
            if (cbSkipInfo.isSelected())
                exportConfigs.addSkipField(Item.Field.INFO);
            if (cbSkipPrivy.isSelected())
                exportConfigs.addSkipField(Item.Field.PRIVY);
            if (cbSkipTags.isSelected())
                exportConfigs.addSkipField(Item.Field.TAGS);
            if (cbSkipDateAdd.isSelected())
                exportConfigs.addSkipField(Item.Field.DATE_ADD);
            if (cbSkipDateMod.isSelected())
                exportConfigs.addSkipField(Item.Field.DATE_ADD);

            BooleanProperty error = new SimpleBooleanProperty(false);
            Stage waitingStage = createWaitingStage("file.working.export");
            new Thread(() -> {
                try {
                    if (writer != null) {
                        store.exportItems(
                                writer,
                                cbFile.getValue(),
                                exportConfigs,
                                cbExportSelected.getValue()
                                        ? view.getListView().getSelectionModel().getSelectedItems()
                                        : store.getAllItems()
                        );
                        Map<DataFormat, Object> cpContent = new HashMap<>();
                        cpContent.put(DataFormat.PLAIN_TEXT, writer.toString());
                        Platform.runLater(() -> Clipboard.getSystemClipboard().setContent(cpContent));
                    } else
                        store.exportItems(
                                path,
                                cbFile.getValue(),
                                exportConfigs,
                                cbExportSelected.getValue()
                                        ? view.getListView().getSelectionModel().getSelectedItems()
                                        : store.getAllItems()
                        );
                } catch (Exception e) {
                    error.set(true);
                }
                Platform.runLater(waitingStage::hide);
            }).start();
            waitingStage.showAndWait();
            if (error.get()) {
                showError("file.error.title", "file.error.export");
            }

            stage.hide();
        });

        // show
        stage.setResizable(false);
        stage.show();
        btnFile.requestFocus();
    }

    public void showStats() {
        long countItem;
        long countTag;
        long countUnusedTags;
        long countUntaggedItems;
        Item item;
        Tag tag;
        long topTagItemCount;
        Collection<Tag> top5;
        Map<Long, Long> top5Count;
        Map<LocalDate, Long> dateTimeLongMap;

        try {
            countItem = store.getItemCount();
            countTag = store.getTagCount();
            countUnusedTags = store.getUnusedTagCount();
            countUntaggedItems = store.getUntaggedItemCount();
            item = store.getMostTaggedItem();
            tag = store.getMostUsedTag();
            if (tag == null)
                topTagItemCount = -1;
            else
                topTagItemCount = store.getTagItemCount(tag.getId());
            top5 = store.getTop5Tags();
            if (top5 == null)
                top5Count = null;
            else {
                long[] ids = new long[top5.size()];
                int i = 0;
                for (Tag t : top5) {
                    ids[i] = t.getId();
                    i++;
                }
                top5Count = store.getTagItemCounts(ids);
            }
            dateTimeLongMap = store.getDateTimeCountMap();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // counts
        PieChart pcCounts = new PieChart(
                FXCollections.observableArrayList(
                        new PieChart.Data(i18n.getString("stats.count.item"), countItem),
                        new PieChart.Data(i18n.getString("stats.count.tag"), countTag),
                        new PieChart.Data(i18n.getString("stats.count.unused"), countUnusedTags),
                        new PieChart.Data(i18n.getString("stats.count.untagged"), countUntaggedItems)
                )
        );
        pcCounts.setTitle(i18n.getString("stats.title.count"));
        pcCounts.setAnimated(true);
        pcCounts.setLegendSide(Side.LEFT);
        final Label lblVal = new Label();
        lblVal.setMouseTransparent(true);
        lblVal.setStyle("-fx-font-size: 1.5em; -fx-font-weight: bold; -fx-text-fill: white;");
        lblVal.setVisible(false);
        for (PieChart.Data data : pcCounts.getData()) {
            data.getNode().setOnMouseEntered(evt -> {
                lblVal.setText((long) data.getPieValue() + "");
                lblVal.setTranslateX(evt.getSceneX());
                lblVal.setTranslateY(evt.getSceneY() - lblVal.getHeight());
                lblVal.setVisible(true);
            });
            data.getNode().setOnMousePressed(data.getNode().getOnMouseEntered());
            data.getNode().setOnMouseExited(evt -> {
                lblVal.setVisible(false);
                lblVal.setTranslateX(0);
                lblVal.setTranslateY(0);
            });
            data.getNode().setOnMouseMoved(evt -> {
                lblVal.setTranslateX(evt.getSceneX());
                lblVal.setTranslateY(evt.getSceneY() - lblVal.getHeight());
            });
        }

        // top 5
        PieChart pcTop5 = null;
        if (top5 != null) {
            Iterator<Tag> itr = top5.iterator();
            pcTop5 = new PieChart();
            while (itr.hasNext()) {
                Tag t = itr.next();
                PieChart.Data data = new PieChart.Data(t.getName(), top5Count.get(t.getId()));
                pcTop5.getData().add(data);
                data.getNode().setOnMouseEntered(evt -> {
                    lblVal.setText((long) data.getPieValue() + "");
                    lblVal.setTranslateX(evt.getSceneX());
                    lblVal.setTranslateY(evt.getSceneY() - lblVal.getHeight());
                    lblVal.setVisible(true);
                });
                data.getNode().setOnMousePressed(data.getNode().getOnMouseEntered());
                data.getNode().setOnMouseExited(evt -> {
                    lblVal.setVisible(false);
                    lblVal.setTranslateX(0);
                    lblVal.setTranslateY(0);
                });
                data.getNode().setOnMouseMoved(evt -> {
                    lblVal.setTranslateX(evt.getSceneX());
                    lblVal.setTranslateY(evt.getSceneY() - lblVal.getHeight());
                });
            }
            pcTop5.setTitle(i18n.getString("stats.title.top5"));
            pcTop5.setAnimated(true);
            pcTop5.setLegendSide(Side.LEFT);
        }

        // timeline
        AreaChart<String, Number> acTimeline;
        if (dateTimeLongMap == null)
            acTimeline = null;
        else {
            CategoryAxis axisDates = new CategoryAxis();
            axisDates.setTickLabelRotation(30);
            NumberAxis axisNumber = new NumberAxis();
            axisNumber.setTickUnit(1);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            acTimeline = new AreaChart<>(axisDates, axisNumber);
            acTimeline.getData().add(series);
            for (LocalDate date : dateTimeLongMap.keySet()) {
                series.getData().add(new XYChart.Data<>(date.format(DateTimeFormatter.ISO_LOCAL_DATE), dateTimeLongMap.get(date)));
            }
            acTimeline.setAlternativeRowFillVisible(true);
            acTimeline.setTitle(i18n.getString("stats.title.timeline"));
            acTimeline.setAnimated(true);
            acTimeline.setLegendVisible(false);
        }

        // top item
        VBox layoutTopItem = null;
        if (item != null) {
            Label lblTopItem = new Label(i18n.getString("stats.top.item") + ": " + item.getTags().size() + " " + i18n.getString("m.tag"));
            ItemView itemView = new ItemView(item);
            itemView.setEditable(false);
            layoutTopItem = new VBox(lblTopItem, itemView);
        }

        // top tag
        VBox layoutTopTag = null;
        if (tag != null) {
            Label lblTopTag = new Label(i18n.getString("stats.top.tag") + ": " + topTagItemCount + " " + i18n.getString("m.item"));
            Text tagText = new Text(tag.getName());
            layoutTopTag = new VBox(lblTopTag, tagText);
        }

        // buttons
        Button btnDismiss = new Button(i18n.getString("button.dismiss"));
        btnDismiss.setDefaultButton(true);
        btnDismiss.setCancelButton(true);

        // layout
        TabPane tabPane = new TabPane();
        tabPane.setSide(Side.LEFT);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);

        Tab tabCounts = new Tab(i18n.getString("stats.title.count"));
        tabCounts.setContent(pcCounts);
        tabPane.getTabs().add(tabCounts);

        if (top5 != null) {
            Tab tabTop5 = new Tab(i18n.getString("stats.title.top5"));
            tabTop5.setContent(pcTop5);
            tabPane.getTabs().add(tabTop5);
        }

        if (dateTimeLongMap != null) {
            Tab tabTimeline = new Tab(i18n.getString("stats.title.timeline"));
            tabTimeline.setContent(acTimeline);
            tabPane.getTabs().add(tabTimeline);
        }

        if (item != null || tag != null) {
            VBox layoutItemTag = new VBox();
            if (item != null)
                layoutItemTag.getChildren().add(layoutTopItem);
            if (tag != null)
                layoutItemTag.getChildren().add(layoutTopTag);
            Tab tabTopItemTag = new Tab(i18n.getString("stats.title.top-item-tag"));
            tabTopItemTag.setContent(layoutItemTag);
            tabPane.getTabs().add(tabTopItemTag);
        }

        VBox layout = new VBox(
                tabPane,
                btnDismiss
        );
        layout.setAlignment(Pos.CENTER);
        layout.setPrefWidth(700);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Stage stage = Dialogs.createUtilityDialog(view.getStage(), i18n.getString("stats.title"), new Group(layout, lblVal));

        btnDismiss.setOnAction(evt -> stage.hide());
        stage.show();
        btnDismiss.requestFocus();
    }

    public void search(String query) {
        ObservableList<Item> items = FXCollections.observableArrayList();
        items.addListener((ListChangeListener<Item>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    view.getItems().addAll(c.getAddedSubList());
                }
            }
        });

        view.getItems().clear();
        try {
            store.searchItems(items, query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
