/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pw.ahs.app.dleely.model.Item;
import pw.ahs.app.dleely.model.Tag;

import java.util.Arrays;


public class Test extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        ListView<Item> listView = new ListView<>();
        listView.setCellFactory(param -> new ItemListCell(System.out::println));

        Item i1 = new Item("item 1", "ref 1");
        i1.getTags().addAll(Arrays.asList(Tag.getInstance("aaaaaaaaaaa"), Tag.getInstance("bbbbbbbbbbbbbbbb"), Tag.getInstance("cccccccccccc")));
        Item i2 = new Item("item 2", "ref 2");
        Item i3 = new Item("item 3", "ref 3");

        listView.getItems().addAll(i1, i2, i3);
        listView.setEditable(true);
        listView.setPrefWidth(300);
        listView.setOnEditStart(evt -> System.out.println("Editing"));
        listView.setOnEditCancel(evt -> System.out.println("Canceled"));
        listView.setOnEditCommit(evt -> System.out.println(evt.getNewValue()));

        ItemView itemView = new ItemView(i1);
        itemView.setOnTagClick(System.out::println);
        itemView.setOnEditStart(i -> System.out.println("Editing"));
        itemView.setOnEditCancel(i -> System.out.println("Canceled"));
        itemView.setOnEditCommit(i -> System.out.println("Committed"));

        primaryStage.setScene(new Scene(listView));
//        primaryStage.setScene(new Scene(new VBox(itemView, new TextField())));
        primaryStage.show();
        primaryStage.getScene().getStylesheets().add(getClass().getResource("css/item-view-style.css").toExternalForm());
        primaryStage.getScene().getStylesheets().add(getClass().getResource("css/style.css").toExternalForm());
        primaryStage.getScene().getAccelerators().put(KeyCombination.valueOf("shortcut+e"), () -> {
            listView.getSelectionModel().clearAndSelect(0);
            listView.edit(0);
        });

//        MenuButton menuButton = new MenuButton("Menu");
//        Menu menu1 =new Menu("File");
//        Menu menu2 = new Menu("Open");
//        menu1.getItems().addAll(menu2, new MenuItem("Help"));
//        menu2.getItems().addAll(new MenuItem("Copy"), new MenuItem("Paste"));
//        menuButton.getItems().addAll(menu1);
//        primaryStage.setScene(new Scene(new VBox(menuButton)));
//        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
