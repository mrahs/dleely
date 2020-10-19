/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.gui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import pw.ahs.app.dleely.Globals;
import pw.ahs.app.dleely.controller.Controller;
import pw.ahs.app.dleely.model.Item;
import pw.ahs.app.dleely.model.Tag;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ItemView extends StackPane {
    private static final double PREF_WIDTH = 200;


    private final Label lblName;
    private final TextField tfName;

    private final Label lblInfo;
    private final TextField tfInfo;

    private final Label lblRef;
    private final TextField tfRef;

    private final Label lblDateAdd;
    private final Label lblDateMod;

    private final ToggleButton tbPrivy;

    private final TextArea taTags;
    private final FlowPane fpTags;
    private final Map<Tag, Hyperlink> mapTagLink;

//    private SetChangeListener<Tag> tagSetChangeListener;

    private OneParamFunction<Tag> onTagClick;

    private final BooleanProperty editing;
    private final BooleanProperty editable;

    private OneParamFunction<Item> onEditStart;
    private OneParamFunction<Item> onEditCancel;
    private OneParamFunction<Item> onEditCommit;

    public ItemView(Item item) {
        this();
        init(item);
    }

    public ItemView() {
        // name
        lblName = new Label();
        lblName.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lblName.getStyleClass().add("item-name");
        lblName.setFocusTraversable(false);
        tfName = new TextField();
        tfName.setPromptText("Name");
        tfName.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tfName.setVisible(false);
        tfName.getStyleClass().add("item-name");
        StackPane spName = new StackPane(tfName, lblName);

        // ref
        lblRef = new Label();
        lblRef.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lblRef.getStyleClass().add("item-ref");
        lblRef.setFocusTraversable(false);
        tfRef = new TextField();
        tfRef.setPromptText("Ref");
        tfRef.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tfRef.setVisible(false);
        tfRef.getStyleClass().add("item-ref");
        tfRef.setEditable(false);
        StackPane spRef = new StackPane(tfRef, lblRef);

        // info
        lblInfo = new Label();
        lblInfo.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lblInfo.getStyleClass().add("item-info");
        lblInfo.setFocusTraversable(false);
        tfInfo = new TextField();
        tfInfo.setPromptText("Info");
        tfInfo.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tfInfo.setVisible(false);
        tfInfo.getStyleClass().add("item-info");
        StackPane spInfo = new StackPane(tfInfo, lblInfo);

        // privy
        tbPrivy = new ToggleButton();
//        tbPrivy.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tbPrivy.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        tbPrivy.setFocusTraversable(false);
        tbPrivy.setDisable(true);
        Region gfxPrivy = new Region();
        gfxPrivy.getStyleClass().add("graphic");
        tbPrivy.setGraphic(gfxPrivy);
        tbPrivy.getStyleClass().setAll("item-privy");

        // dates
        lblDateAdd = new Label();
        lblDateAdd.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lblDateAdd.getStyleClass().add("item-date-add");
        lblDateAdd.setFocusTraversable(false);
        lblDateMod = new Label();
        lblDateMod.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lblDateMod.getStyleClass().add("item-date-mod");
        lblDateMod.setFocusTraversable(false);
        HBox hbDates = new HBox();
        Region filler = new Region();
        filler.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        HBox.setHgrow(filler, Priority.ALWAYS);
        hbDates.getChildren().addAll(lblDateAdd, filler, lblDateMod);

        // tags
        taTags = new TextArea();
        taTags.setPromptText("Tags");
        taTags.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        taTags.setVisible(false);
        taTags.setPrefRowCount(1);
        taTags.setWrapText(true);
        taTags.getStyleClass().add("item-tags");
        UIHelper.hackTextAreaTab(taTags);
        fpTags = new FlowPane();
        fpTags.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        fpTags.setVgap(5);
        fpTags.setHgap(5);
        fpTags.getStyleClass().add("item-tags");
        fpTags.setFocusTraversable(false);
        fpTags.setPrefWrapLength(PREF_WIDTH);
        StackPane spTags = new StackPane(taTags, fpTags);
        mapTagLink = new HashMap<>();

        onTagClick = null;
//        tagSetChangeListener = change -> {
//            if (change.wasAdded()) {
//                Tag t = change.getElementAdded();
//
//                Hyperlink hyperlink = generateTagLink(t);
//
//                fpTags.getChildren().add(hyperlink);
//                mapTagLink.put(t, hyperlink);
//            } else if (change.wasRemoved()) {
//                fpTags.getChildren().remove(mapTagLink.get(change.getElementRemoved()));
//            }
//        };
        onEditStart = null;
        onEditCancel = null;
        onEditCommit = null;

        editing = new SimpleBooleanProperty(false);
        editable = new SimpleBooleanProperty(true);

        HBox.setHgrow(spName, Priority.ALWAYS);
        VBox.setVgrow(spTags, Priority.ALWAYS);
        VBox layout = new VBox(
                new HBox(spName, tbPrivy),
                spRef,
                spInfo,
                spTags,
                hbDates
        );
        layout.setSpacing(5);

        super.getChildren().add(layout);
        super.getStyleClass().add("item-view");
        super.setMinSize(235, 130);
        super.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        super.setPrefWidth(PREF_WIDTH);
//        registerEvents();
    }

    private void registerEvents() {
        super.setOnMouseClicked(evt -> {
            if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() > 1) {
                startEdit();
            }
        });

        super.setOnKeyReleased(evt -> {
            if (evt.getCode() == KeyCode.F2) {
                startEdit();
            } else if (evt.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            } else if (evt.getCode() == KeyCode.ENTER) {
                commitEdit();
            }
        });

        ChangeListener<Boolean> focusChangeListener = (observable, oldValue, newValue) -> {
            if (tfName.isFocused() || tfRef.isFocused() || tbPrivy.isFocused() || tfInfo.isFocused() || taTags.isFocused())
                return;
            if (oldValue) {
                cancelEdit();
            } else {
                tfName.requestFocus();
            }
        };
        tfName.focusedProperty().addListener(focusChangeListener);
        tfRef.focusedProperty().addListener(focusChangeListener);
        tfInfo.focusedProperty().addListener(focusChangeListener);
        taTags.focusedProperty().addListener(focusChangeListener);
        tbPrivy.focusedProperty().addListener(focusChangeListener);
    }

    private Hyperlink generateTagLink(Tag t) {
        Hyperlink hyperlink = new Hyperlink(t.getName());
        hyperlink.getStyleClass().add("tag-link");
        hyperlink.setOnAction(evt -> {
//            evt.consume();
            if (onTagClick == null) return;
            onTagClick.apply(t);
        });
        return hyperlink;
    }

    public void init(Item item) {
        lblName.setText("");
        lblRef.setText("");
        lblInfo.setText("");
        tbPrivy.setSelected(false);
        lblDateAdd.setText("");
        lblDateMod.setText("");
        fpTags.getChildren().clear();
        mapTagLink.clear();

        if (item == null) return;

        lblName.setText(item.getName());
        lblRef.setText(item.getRef());
        lblInfo.setText(item.getInfo());
        tbPrivy.setSelected(item.getPrivy());
        lblDateAdd.setText(item.getDateAddText());
        lblDateMod.setText(item.getDateModText());
        for (Tag t : item.getTags()) {
            Hyperlink hyperlink = generateTagLink(t);
            fpTags.getChildren().add(hyperlink);
            mapTagLink.put(t, hyperlink);
        }
    }

    public void setOnTagClick(OneParamFunction<Tag> onTagClick) {
        this.onTagClick = onTagClick;
    }

    public Item createItem() {
        if (lblRef.getText().trim().isEmpty()) return null;
        Item item = new Item(lblName.getText(), lblRef.getText());
        item.setInfo(lblInfo.getText());
        item.setPrivy(tbPrivy.isSelected());
        item.getTags().addAll(mapTagLink.keySet());
        item.setDateAdd(LocalDateTime.parse(lblDateAdd.getText(), Globals.DATE_TIME_FORMATTER));
        item.setDateMod(LocalDateTime.parse(lblDateMod.getText(), Globals.DATE_TIME_FORMATTER));
        return item;
    }

    public OneParamFunction<Tag> getOnTagClick() {
        return onTagClick;
    }

    public boolean getEditing() {
        return editing.get();
    }

    public BooleanProperty editingProperty() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing.set(editing);
    }

    public boolean getEditable() {
        return editable.get();
    }

    public BooleanProperty editableProperty() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable.set(editable);
    }

    public void startEdit() {
        if (!editable.get()) return;
        if (editing.get()) return;
        // this must be executed ASAP, because this method might be called multiple times
        this.editing.set(true);

        // name
        tfName.setText(lblName.getText());
        lblName.setVisible(false);
        tfName.setVisible(true);

        // ref
        tfRef.setText(lblRef.getText());
        lblRef.setVisible(false);
        tfRef.setVisible(true);

        // info
        tfInfo.setText(lblInfo.getText());
        lblInfo.setVisible(false);
        tfInfo.setVisible(true);

        // privy
        tbPrivy.setUserData(tbPrivy.isSelected());
        tbPrivy.setFocusTraversable(true);
        tbPrivy.setDisable(false);

        // tags
        taTags.setText(Controller.util.joinArray(mapTagLink.keySet().toArray(), " "));
        fpTags.setVisible(false);
        taTags.setVisible(true);

        tfName.requestFocus();

        if (onEditStart != null) onEditStart.apply(createItem());
    }

    public void cancelEdit() {
        if (!editing.get()) return;
        // this must be executed ASAP, because this method might be called multiple times
        this.editing.set(false);

        // name
        tfName.setVisible(false);
        lblName.setVisible(true);

        // ref
        tfRef.setVisible(false);
        lblRef.setVisible(true);

        // info
        tfInfo.setVisible(false);
        lblInfo.setVisible(true);

        // privy
        tbPrivy.setSelected(((boolean) tbPrivy.getUserData()));
        tbPrivy.setFocusTraversable(false);
        tbPrivy.setDisable(true);

        // tags
        taTags.setVisible(false);
        fpTags.setVisible(true);

        // clear
        tfName.clear();
        tfInfo.clear();
        taTags.clear();

        if (onEditCancel != null) onEditCancel.apply(createItem());
    }

    public void commitEdit() {
        if (!editing.get()) return;
        // this must be executed ASAP, because this method might be called multiple times
        this.editing.set(false);

        // name
        lblName.setText(tfName.getText().trim());
        tfName.setVisible(false);
        lblName.setVisible(true);

        // ref
        tfRef.setVisible(false);
        lblRef.setVisible(true);

        // info
        lblInfo.setText(tfInfo.getText());
        tfInfo.setVisible(false);
        lblInfo.setVisible(true);

        // privy
        tbPrivy.setFocusTraversable(false);
        tbPrivy.setDisable(true);

        // tags
        String[] tagsNames = taTags.getText().split("\\s");
        fpTags.getChildren().clear();
        mapTagLink.clear();
        for (String tag : tagsNames) {
            if (!Tag.isValidTagName(tag)) continue;
            Tag t = Tag.getInstance(tag);
            Hyperlink hyperlink = generateTagLink(t);
            fpTags.getChildren().add(hyperlink);
            mapTagLink.put(t, hyperlink);
        }
        taTags.setVisible(false);
        fpTags.setVisible(true);

        lblDateMod.setText(LocalDateTime.now().format(Globals.DATE_TIME_FORMATTER));

        if (onEditCommit != null) onEditCommit.apply(createItem());
    }

    public void setOnEditStart(OneParamFunction<Item> onEditStart) {
        this.onEditStart = onEditStart;
    }

    public void setOnEditCancel(OneParamFunction<Item> onEditCancel) {
        this.onEditCancel = onEditCancel;
    }

    public void setOnEditCommit(OneParamFunction<Item> onEditCommit) {
        this.onEditCommit = onEditCommit;
    }
}
