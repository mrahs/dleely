/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.gui.helper;

import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Region;
import pw.ahs.app.dleely.command.AddItemsCommand;
import pw.ahs.app.dleely.command.DeleteItemsCommand;
import pw.ahs.app.dleely.command.ICommand;
import pw.ahs.app.dleely.controller.Controller;
import pw.ahs.app.dleely.controller.IOController;
import pw.ahs.app.dleely.controller.SettingsController;
import pw.ahs.app.dleely.gui.*;
import pw.ahs.app.dleely.model.Item;
import pw.ahs.app.fxsimplecontrols.Dialogs;

import java.util.ArrayList;

import static pw.ahs.app.dleely.Globals.i18n;
import static pw.ahs.app.dleely.Globals.view;

public class MenuHelper {
    private static MenuHelper instance = null;

    public static MenuHelper getInstance() {
        if (instance == null)
            instance = new MenuHelper();
        return instance;
    }

    private MenuHelper() {
    }

    private Menu mnuFileOpenRecent;

    public void setOpenRecentMenuVisible(boolean state) {
        mnuFileOpenRecent.setVisible(state);
    }

    public void init(
            MenuButton menuBtn,
            TextField searchBox,
            ListView<Item> listView
    ) {
        Region gfxMenuButton = new Region();
        gfxMenuButton.getStyleClass().addAll("graphic");
        menuBtn.setGraphic(gfxMenuButton);
        menuBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        // File
        MenuItem mnuFileNew = new MenuItem();
        mnuFileNew.setAccelerator(KeyCombination.valueOf("shortcut+n"));
        mnuFileNew.setOnAction(evt -> UIHelper.io.newFile());

        MenuItem mnuFileOpen = new MenuItem();
        mnuFileOpen.setAccelerator(KeyCombination.valueOf("shortcut+o"));
        mnuFileOpen.setOnAction(evt -> UIHelper.io.open(""));

        mnuFileOpenRecent = new Menu();
        mnuFileOpenRecent.setAccelerator(KeyCombination.valueOf("shortcut+shift+o"));
        for (int i = 0; i < SettingsController.RECENT_FILES_LIMIT; ++i) {
            mnuFileOpenRecent.getItems().add(new MenuItem());
        }
        Controller.settings.addListener(SettingsController.Setting.sFile_Path, (oldValue, newValue) -> {
            String[] paths = newValue.split(";");
            int k = 0;
            for (; k < SettingsController.RECENT_FILES_LIMIT && k < paths.length; k++) {
                String path = paths[k];
                int i = path.lastIndexOf(IOController.NAME_SEPARATOR) + 1;
                int j = path.indexOf('.', i);
                if (j < 0) j = path.length();
                mnuFileOpenRecent.getItems().get(k).setText(path.substring(i, j));
                mnuFileOpenRecent.getItems().get(k).setOnAction(evt2 -> UIHelper.io.open(path));
                mnuFileOpenRecent.getItems().get(k).setVisible(true);
            }
            for (; k < SettingsController.RECENT_FILES_LIMIT; k++) {
                mnuFileOpenRecent.getItems().get(k).setVisible(false);
            }
        });

        MenuItem mnuFileSave = new MenuItem();
        mnuFileSave.setAccelerator(KeyCombination.valueOf("shortcut+s"));
        mnuFileSave.setOnAction(evt -> UIHelper.io.save(false));

        MenuItem mnuFileSaveAs = new MenuItem();
        mnuFileSaveAs.setAccelerator(KeyCombination.valueOf("shortcut+shift+s"));
        mnuFileSaveAs.setOnAction(evt -> UIHelper.io.save(true));

        MenuItem mnuImport = new MenuItem();
        mnuImport.setAccelerator(KeyCombination.valueOf("shortcut+i"));
        mnuImport.setOnAction(evt -> UIHelper.io.importItems(false));

        MenuItem mnuExport = new MenuItem();
        mnuExport.setAccelerator(KeyCombination.valueOf("shortcut+e"));
        mnuExport.setOnAction(evt -> UIHelper.io.exportItems(false));

        MenuItem mnuFileClose = new MenuItem();
        mnuFileClose.setAccelerator(KeyCombination.valueOf("shortcut+w"));
        mnuFileClose.setOnAction(evt -> UIHelper.io.close());

        Menu menuFile = new Menu();
        menuFile.getItems().addAll(
                mnuFileNew,
                mnuFileOpen,
                mnuFileOpenRecent,
                mnuFileSave,
                mnuFileSaveAs,
                new SeparatorMenuItem(),
                mnuImport,
                mnuExport,
                new SeparatorMenuItem(),
                mnuFileClose
        );

        // Item
        MenuItem mnuItemNew = new MenuItem();
        mnuItemNew.setAccelerator(KeyCombination.valueOf("shortcut+="));
        mnuItemNew.setOnAction(evt -> {
            String ref = Dialogs.showTextInputDialog(
                    view.getStage(),
                    i18n.getString("m.new-item.title"),
                    i18n.getString("m.new-item.msg"),
                    "",
                    i18n.getString("button.add"),
                    i18n.getString("button.cancel")
            );
            if (ref.isEmpty()) return;

            Item item = new Item(i18n.getString("m.new"), ref);
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
        });

        MenuItem mnuItemDel = new MenuItem();
        mnuItemDel.setAccelerator(KeyCombination.valueOf("delete"));
        mnuItemDel.setOnAction(evt -> {
            ICommand cmd = new DeleteItemsCommand(new ArrayList<>(listView.getSelectionModel().getSelectedItems()));
            Controller.undoRedo.executeThenPushCommand(cmd);
        });
        mnuItemDel.disableProperty().bind(new BooleanBinding() {
            {
                bind(listView.getSelectionModel().selectedIndexProperty());
            }

            @Override
            protected boolean computeValue() {
                return listView.getSelectionModel().getSelectedIndex() < 0;
            }
        });

        MenuItem mnuItemEdit = new MenuItem();
        mnuItemEdit.setAccelerator(KeyCombination.valueOf("f2"));
        mnuItemEdit.setOnAction(evt -> listView.edit(listView.getSelectionModel().getSelectedIndex()));
        mnuItemEdit.disableProperty().bind(new BooleanBinding() {
            {
                bind(listView.getSelectionModel().selectedIndexProperty());
            }

            @Override
            protected boolean computeValue() {
                return listView.getSelectionModel().getSelectedIndex() < 0
                        || listView.getSelectionModel().getSelectedIndices().size() != 1;
            }
        });

        MenuItem mnuItemCopy = new MenuItem();
        mnuItemCopy.setAccelerator(KeyCombination.valueOf("shortcut+c"));
        mnuItemCopy.setOnAction(evt -> UIHelper.io.exportItems(true));
        mnuItemCopy.disableProperty().bind(new BooleanBinding() {
            {
                bind(listView.getSelectionModel().selectedIndexProperty());
            }

            @Override
            protected boolean computeValue() {
                return listView.getSelectionModel().getSelectedIndex() < 0;
            }
        });

        MenuItem mnuItemPaste = new MenuItem();
        mnuItemPaste.setAccelerator(KeyCombination.valueOf("shortcut+v"));
        mnuItemPaste.setOnAction(evt -> UIHelper.io.importItems(true));

        MenuItem mnuUndo = new MenuItem();
        mnuUndo.setAccelerator(KeyCombination.valueOf("shortcut+z"));
        mnuUndo.setOnAction(evt -> Controller.undoRedo.undo());
        Controller.undoRedo.addUndoListener(c -> {
            if (Controller.undoRedo.noUndo()) {
                mnuUndo.setText(i18n.getString("menu.undo"));
                mnuUndo.setDisable(true);
            } else {
                ICommand lastCmd = Controller.undoRedo.peekUndo();
                mnuUndo.setText(i18n.getString("menu.undo") + " " + i18n.getString(lastCmd.getDesc()));
                mnuUndo.setDisable(false);
            }
        });
        mnuUndo.setDisable(true);

        MenuItem mnuRedo = new MenuItem();
        mnuRedo.setAccelerator(KeyCombination.valueOf("shortcut+shift+z"));
        mnuRedo.setOnAction(evt -> Controller.undoRedo.redo());
        Controller.undoRedo.addRedoListener(c -> {
            if (Controller.undoRedo.noRedo()) {
                mnuRedo.setText(i18n.getString("menu.redo"));
                mnuRedo.setDisable(true);
            } else {
                ICommand lastCmd = Controller.undoRedo.peekRedo();
                mnuRedo.setText(i18n.getString("menu.redo") + " " + i18n.getString(lastCmd.getDesc()));
                mnuRedo.setDisable(false);
            }
        });
        mnuRedo.setDisable(true);

        Menu menuItem = new Menu();
        menuItem.getItems().addAll(
                mnuItemNew, mnuItemDel, mnuItemEdit,
                new SeparatorMenuItem(),
                mnuItemCopy, mnuItemPaste,
                new SeparatorMenuItem(),
                mnuUndo, mnuRedo
        );

        // Search
        MenuItem mnuSearch = new MenuItem();
        mnuSearch.setAccelerator(KeyCombination.valueOf("shortcut+f"));
        mnuSearch.setOnAction(evt -> {
            searchBox.selectAll();
            searchBox.requestFocus();
        });

        MenuItem mnuSearchSimilar = new MenuItem();
        mnuSearchSimilar.setAccelerator(KeyCombination.valueOf("shortcut+shift+f"));
        mnuSearchSimilar.setOnAction(evt -> searchBox.setText(listView.getSelectionModel().getSelectedItem().getTagsText()));
        mnuSearchSimilar.disableProperty().bind(new BooleanBinding() {
            {
                bind(listView.getSelectionModel().selectedIndexProperty());
            }

            @Override
            protected boolean computeValue() {
                return listView.getSelectionModel().getSelectedIndex() < 0
                        || listView.getSelectionModel().getSelectedIndices().size() != 1;
            }
        });

        Menu menuSearch = new Menu();
        menuSearch.getItems().addAll(mnuSearch, mnuSearchSimilar);

        // Help
        MenuItem mnuHelpFile = new MenuItem();
        mnuHelpFile.setAccelerator(KeyCombination.valueOf("f1"));
        mnuHelpFile.setDisable(true);

        MenuItem mnuAbout = new MenuItem();
        mnuAbout.setAccelerator(KeyCombination.valueOf("shortcut+f1"));
        mnuAbout.setOnAction(evt -> AboutHelper.getInstance().showAbout());

        MenuItem mnuSplash = new MenuItem();
        mnuSplash.setAccelerator(KeyCombination.valueOf("shortcut+shift+f1"));
        mnuSplash.setOnAction(evt -> Splash.show());

        MenuItem mnuUpdate = new MenuItem();
        mnuUpdate.setOnAction(evt -> view.checkForUpdate(true));
        mnuUpdate.setDisable(true);

        MenuItem mnuFeedback = new MenuItem();
        mnuFeedback.setDisable(true);

        Menu menuHelp = new Menu();
        menuHelp.getItems().addAll(mnuHelpFile, mnuAbout, mnuFeedback, mnuSplash, mnuUpdate);

        // Settings
        MenuItem mnuSettings = new MenuItem();
        mnuSettings.setAccelerator(KeyCombination.valueOf("shortcut+p"));
        mnuSettings.setOnAction(evt -> UIHelper.settings.show());

        // Stats
        MenuItem mnuStats = new MenuItem();
        mnuStats.setAccelerator(KeyCombination.valueOf("shortcut+t"));
        mnuStats.setOnAction(evt -> UIHelper.io.showStats());

        // widget
        MenuItem mnuWidget = new MenuItem();
        mnuWidget.setAccelerator(KeyCombination.valueOf("shortcut+g"));
        mnuWidget.setOnAction(evt -> new Widget(view.getStage().getIcons()).show());

        // Exit
        MenuItem mnuExit = new MenuItem();
        mnuExit.setAccelerator(KeyCombination.valueOf("alt+f4"));
        mnuExit.setOnAction(evt -> view.exit());

        menuBtn.getItems().addAll(
                menuFile,
                menuItem,
                menuSearch,
                mnuSettings,
                mnuStats,
                mnuWidget,
                menuHelp,
                mnuExit
        );

        OneParamFunction<String> menuLangListener = newLang -> {
            mnuFileNew.setText(i18n.getString("menu.new"));
            mnuFileOpen.setText(i18n.getString("menu.open"));
            mnuFileOpenRecent.setText(i18n.getString("menu.open-recent"));
            mnuFileSave.setText(i18n.getString("menu.save"));
            mnuFileSaveAs.setText(i18n.getString("menu.save-as"));
            mnuFileClose.setText(i18n.getString("menu.close"));
            menuFile.setText(i18n.getString("menu.file"));
            mnuItemNew.setText(i18n.getString("menu.new"));
            mnuItemDel.setText(i18n.getString("menu.del"));
            mnuItemEdit.setText(i18n.getString("menu.edit"));
            menuItem.setText(i18n.getString("menu.item"));
            menuSearch.setText(i18n.getString("menu.search"));
            mnuSearch.setText(i18n.getString("menu.search"));
            mnuSearchSimilar.setText(i18n.getString("menu.similar"));
            mnuHelpFile.setText(i18n.getString("menu.help-file"));
            mnuAbout.setText(i18n.getString("menu.about"));
            mnuSplash.setText(i18n.getString("menu.splash"));
            menuHelp.setText(i18n.getString("menu.help"));
            mnuSettings.setText(i18n.getString("menu.settings"));
            mnuExit.setText(i18n.getString("menu.exit"));
            mnuImport.setText(i18n.getString("menu.import"));
            mnuExport.setText(i18n.getString("menu.export"));
            mnuItemCopy.setText(i18n.getString("menu.copy"));
            mnuItemPaste.setText(i18n.getString("menu.paste"));
            mnuUndo.setText(i18n.getString("menu.undo"));
            mnuRedo.setText(i18n.getString("menu.redo"));
            mnuStats.setText(i18n.getString("menu.stats"));
            mnuUpdate.setText(i18n.getString("menu.update"));
            mnuFeedback.setText(i18n.getString("menu.feedback"));
            mnuWidget.setText(i18n.getString("menu.widget"));
        };
        menuLangListener.apply(null);

        Controller.curLang.addListener(menuLangListener);
    }
}
