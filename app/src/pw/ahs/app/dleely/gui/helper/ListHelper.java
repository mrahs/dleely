/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.gui.helper;

import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import pw.ahs.app.dleely.command.EditItemCommand;
import pw.ahs.app.dleely.command.ICommand;
import pw.ahs.app.dleely.controller.Controller;
import pw.ahs.app.dleely.gui.ItemListCell;
import pw.ahs.app.dleely.gui.OneParamFunction;
import pw.ahs.app.dleely.model.Item;
import pw.ahs.app.dleely.model.Tag;

import static pw.ahs.app.dleely.Globals.i18n;
import static pw.ahs.app.dleely.Globals.view;

public class ListHelper {
    private static ListHelper instance = null;

    public static ListHelper getInstance() {
        if (instance == null)
            instance = new ListHelper();
        return instance;
    }

    private ListHelper() {
    }

    public void init(ListView<Item> listView, TextField searchBox) {
        Label lblListPlaceHolderTitle = new Label("");
        lblListPlaceHolderTitle.setId("list-place-holder-title");
        Label lblListPlaceHolderMsg = new Label("");
        VBox listPlaceHolder = new VBox(
                lblListPlaceHolderTitle,
                new Separator(),
                lblListPlaceHolderMsg
        );
        listPlaceHolder.setSpacing(5);
        listPlaceHolder.setMaxHeight(Region.USE_PREF_SIZE);
        listPlaceHolder.setMaxWidth(Region.USE_PREF_SIZE);

        OneParamFunction<Tag> tagClickHandler = tag -> {
            searchBox.appendText(tag.getName() + " ");
            searchBox.requestFocus();
        };

        listView.setItems(view.getItems());
        listView.setPlaceholder(listPlaceHolder);
        listView.setCellFactory(v -> new ItemListCell(tagClickHandler));
        listView.setEditable(true);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

//        listView.setOnEditStart(evt -> System.out.println("Editing"));
//        listView.setOnEditCancel(evt -> System.out.println("Canceled"));
        listView.setOnEditCommit(evt -> {
            ICommand cmd = new EditItemCommand(evt.getSource().getItems().get(evt.getIndex()), evt.getNewValue());
            Controller.undoRedo.executeThenPushCommand(cmd);
        });

        OneParamFunction<String> listLangListener = newLang -> {
            lblListPlaceHolderTitle.setText(i18n.getString("m.list-place-holder.title"));
            lblListPlaceHolderMsg.setText(i18n.getString("m.list-place-holder.msg"));
        };
        listLangListener.apply(null);
        Controller.curLang.addListener(listLangListener);
    }
}
