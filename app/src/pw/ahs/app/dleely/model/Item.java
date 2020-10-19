/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import pw.ahs.app.dleely.controller.Controller;

import java.time.LocalDateTime;
import java.util.TreeSet;

import static pw.ahs.app.dleely.Globals.DATE_TIME_FORMATTER;
import static pw.ahs.app.dleely.Globals.NULL_ID;

public class Item {

    private final LongProperty id;
    private final StringProperty name;
    private final ReadOnlyStringWrapper ref;
    private final StringProperty info;
    private final ObservableSet<Tag> tags;
    private final ReadOnlyStringWrapper tagsText;
    private final StringProperty tagsSep;
    private final BooleanProperty privy;
    private final ObjectProperty<LocalDateTime> dateAdd;
    private final ReadOnlyStringWrapper dateAddText;
    private final ObjectProperty<LocalDateTime> dateMod;
    private final ReadOnlyStringWrapper dateModText;

    public Item(String name, String ref) {
        this.id = new SimpleLongProperty() {
            @Override
            public void setValue(Number v) {
                if (v == null) throw new NullPointerException();
                super.setValue(v);
            }
        };
        this.name = new SimpleStringProperty() {
            @Override
            public void set(String newValue) {
                newValue = newValue.trim();
                if (newValue.isEmpty()) throw new IllegalArgumentException("name must not be null nor empty");
                super.set(newValue);
                setDateMod(LocalDateTime.now());
            }
        };
        this.ref = new ReadOnlyStringWrapper() {
            @Override
            public void set(String newValue) {
                newValue = newValue.trim();
                if (newValue.isEmpty()) throw new IllegalArgumentException("ref must not be null nor empty");
                super.set(newValue);
            }
        };
        this.info = new SimpleStringProperty() {
            @Override
            public void set(String newValue) {
                super.set(newValue.trim());
                setDateMod(LocalDateTime.now());
            }
        };
        this.tags = FXCollections.observableSet(new TreeSet<>());
        this.tags.addListener((SetChangeListener<Tag>) change -> setDateMod(LocalDateTime.now()));
        this.tagsText = new ReadOnlyStringWrapper();
        this.tagsSep = new SimpleStringProperty() {
            @Override
            public void set(String newValue) {
                if (newValue == null) throw new NullPointerException();
                super.set(newValue);
            }
        };
        this.privy = new SimpleBooleanProperty() {
            @Override
            public void setValue(Boolean v) {
                if (v == null) throw new NullPointerException();
                super.setValue(v);
            }

            @Override
            public void set(boolean newValue) {
                super.set(newValue);
                setDateMod(LocalDateTime.now());
            }
        };
        this.dateAdd = new SimpleObjectProperty<LocalDateTime>() {
            @Override
            public void set(LocalDateTime newValue) {
                if (newValue == null)
                    super.set(LocalDateTime.now());
                else
                    super.set(newValue);
            }
        };
        this.dateAddText = new ReadOnlyStringWrapper();
        this.dateAdd.addListener((observable, oldValue, newValue) -> this.dateAddText.set(newValue.format(DATE_TIME_FORMATTER)));

        this.dateMod = new SimpleObjectProperty<LocalDateTime>() {
            @Override
            public void set(LocalDateTime newValue) {
                if (newValue == null)
                    super.set(LocalDateTime.now());
                else
                    super.set(newValue);
            }
        };
        this.dateModText = new ReadOnlyStringWrapper();
        this.dateMod.addListener((observable, oldValue, newValue) -> this.dateModText.set(newValue.format(DATE_TIME_FORMATTER)));


        this.tags.addListener((SetChangeListener<Tag>) change ->
                tagsText.set(Controller.util.joinCollection(tags, tagsSep.get())));
        this.tagsSep.addListener((observable, oldValue, newValue) ->
                tagsText.set(Controller.util.joinCollection(tags, newValue)));

        setId(NULL_ID);
        setName(name);
        this.ref.set(ref);
        setInfo("");
        tagsText.set("");
        setTagsSep(" ");
        setPrivy(false);
        setDateAdd(LocalDateTime.now());
        setDateMod(getDateAdd());
    }

    public long getId() {
        return id.get();
    }

    public void setId(long id) {
        this.id.set(id);
    }

    public LongProperty idProperty() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getRef() {
        return ref.get();
    }

    public ReadOnlyStringProperty refProperty() {
        return ref.getReadOnlyProperty();
    }

    public String getInfo() {
        return info.get();
    }

    public void setInfo(String info) {
        this.info.set(info);
    }

    public StringProperty infoProperty() {
        return info;
    }

    public boolean getPrivy() {
        return privy.get();
    }

    public void setPrivy(boolean privy) {
        this.privy.set(privy);
    }

    public BooleanProperty privyProperty() {
        return privy;
    }

    public LocalDateTime getDateAdd() {
        return dateAdd.get();
    }

    public void setDateAdd(LocalDateTime dateAdd) {
        this.dateAdd.set(dateAdd);
    }

    public ObjectProperty<LocalDateTime> dateAddProperty() {
        return dateAdd;
    }

    public String getDateAddText() {
        return dateAddText.get();
    }

    public ReadOnlyStringProperty dateAddTextProperty() {
        return dateAddText.getReadOnlyProperty();
    }

    public LocalDateTime getDateMod() {
        return dateMod.get();
    }

    public void setDateMod(LocalDateTime dateMod) {
        this.dateMod.set(dateMod);
    }

    public ObjectProperty<LocalDateTime> dateModProperty() {
        return dateMod;
    }

    public String getDateModText() {
        return dateModText.get();
    }

    public ReadOnlyStringProperty dateModTextProperty() {
        return dateModText.getReadOnlyProperty();
    }

    public ObservableSet<Tag> getTags() {
        return tags;
    }

    public String getTagsText() {
        return tagsText.get();
    }

    public String getTagsText(String sep) {
        setTagsSep(sep);
        return getTagsText();
    }

    public ReadOnlyStringProperty tagsTextProperty() {
        return tagsText.getReadOnlyProperty();
    }

    public String getTagsSep() {
        return tagsSep.get();
    }

    public StringProperty tagsSepProperty() {
        return tagsSep;
    }

    public void setTagsSep(String tagsSep) {
        this.tagsSep.set(tagsSep);
    }

//    public void copyTo(Item other) {
//        other.id.set(this.id.get());
//        other.name.set(this.name.get());
//        other.info.set(this.info.get());
//        other.ref.set(this.ref.get());
//        other.privy.set(this.privy.get());
//        other.tags.clear();
//        other.tags.addAll(this.tags);
//        other.dateAdd.set(this.dateAdd.get());
//        other.dateMod.set(this.dateMod.get());
//    }
//
//    public Item cloneNew() {
//        Item clone = new Item("Null", "Null");
//        this.copyTo(clone);
//        return clone;
//    }

    @Override
    public String toString() {
        return Controller.util.joinArray(new Object[]{
                name.get(),
                ref.get(),
                info.get(),
                getTagsText(" "),
                privy.get(),
                DATE_TIME_FORMATTER.format(dateAdd.get()),
                DATE_TIME_FORMATTER.format(dateMod.get())
        }, ", ");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        return ref.get().equals(item.ref.get());

    }

    @Override
    public int hashCode() {
        return ref.get().hashCode();
    }

    public static enum Field {
        ID, NAME, REF, INFO, PRIVY, TAGS, DATE_ADD, DATE_MODIFY
    }
}
