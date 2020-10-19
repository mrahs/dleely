/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.gui;

import javafx.scene.control.ListCell;
import javafx.scene.input.KeyCode;
import pw.ahs.app.dleely.model.Item;
import pw.ahs.app.dleely.model.Tag;

public class ItemListCell extends ListCell<Item> {
    private boolean initialized = false;
    private ItemView itemView = null;
    private OneParamFunction<Tag> onTagClick = null;

    public ItemListCell() {
        super.setText(null);
        super.getStyleClass().add("item-list-cell");
        super.setOnKeyReleased(evt -> {
            if (evt.getCode() == KeyCode.ENTER) {
                itemView.commitEdit();
            }
        });
    }

    public ItemListCell(OneParamFunction<Tag> onTagClick) {
        this();
        this.onTagClick = onTagClick;
    }

    @Override
    protected void updateItem(Item item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            super.setGraphic(null);
            return;
        }

        if (initialized) {
            itemView.init(item);
            super.setGraphic(itemView);
            return;
        }

        itemView = new ItemView(item);
        itemView.setEditable(super.getListView().isEditable() && super.isEditable());
        itemView.setOnEditCommit(this::commitEdit);
        itemView.setOnTagClick(onTagClick);
        super.setGraphic(itemView);
        super.setMinWidth(itemView.getMinWidth());
        super.setPrefWidth(itemView.getPrefWidth());
        initialized = true;
    }

    @Override
    public void startEdit() {
        if (super.getListView().isEditable() && super.isEditable() && !super.isEditing() && !super.isEmpty()) {
            super.startEdit();
            itemView.startEdit();
        }
    }

    @Override
    public void cancelEdit() {
        if (super.isEditing()) {
            super.cancelEdit();
            itemView.cancelEdit();
        }
    }

    @Override
    public void commitEdit(Item newValue) {
        if (super.isEditing()) {
            super.commitEdit(newValue);
        }
    }
}
