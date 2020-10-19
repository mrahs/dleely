/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.model;

import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static pw.ahs.app.dleely.Globals.NULL_ID;

public class Tag implements Comparable<Tag> {

    private static final Pattern PATTERN_TAG_NAME = Pattern.compile("(?U)^\\w+$");
    private static final Map<Long, Tag> ID_TAG_MAP = new HashMap<>();
    private static final Map<String, Tag> NAME_TAG_MAP = new HashMap<>();

    private final LongProperty id;
    private final ReadOnlyStringWrapper name;
    private final ObjectProperty<LocalDateTime> dateAdd;
    private final ObjectProperty<LocalDateTime> dateMod;
    private final LongProperty parentId;
    private final LongProperty groupId;

    private Tag(String name) {
        this.id = new SimpleLongProperty(NULL_ID) {
            @Override
            public void set(long newValue) {
                super.set(newValue);
                ID_TAG_MAP.put(newValue, Tag.this);
            }

            @Override
            public void setValue(Number v) {
                if (v == null)
                    throw new NullPointerException();
                else
                    super.setValue(v);
            }
        };
        this.name = new ReadOnlyStringWrapper("Null");
        this.dateAdd = new SimpleObjectProperty<LocalDateTime>(LocalDateTime.now()) {
            @Override
            public void set(LocalDateTime newValue) {
                if (newValue == null)
                    super.set(LocalDateTime.now());
                else
                    super.set(newValue);
            }
        };
        this.dateMod = new SimpleObjectProperty<LocalDateTime>(dateAdd.get()) {
            @Override
            public void set(LocalDateTime newValue) {
                if (newValue == null)
                    super.set(LocalDateTime.now());
                else
                    super.set(newValue);
            }
        };
        this.parentId = new SimpleLongProperty(NULL_ID) {
            @Override
            public void setValue(Number v) {
                if (v == null)
                    throw new NullPointerException();
                else
                    super.setValue(v);
            }
        };
        this.groupId = new SimpleLongProperty(NULL_ID) {
            @Override
            public void setValue(Number v) {
                if (v == null)
                    throw new NullPointerException();
                else
                    super.setValue(v);
            }
        };

        this.name.set(name);
    }

    /**
     * The purpose of this method is to retrieve parent tag by id
     *
     * @param id tag id
     * @return the tag if it exists, null otherwise
     */
    public static Tag getIfExists(long id) {
        return ID_TAG_MAP.get(id);
    }

    /**
     * The purpose of this method is to avoid creating tags with the same name. This is intended to
     * keep parent id and group id fields consistent.
     *
     * @param name the tag name
     * @return the tag that has the specified name if it already exists, a new tag with the specified name otherwise
     */
    public static Tag getInstance(String name) {
        name = checkFixName(name);
        Tag t = NAME_TAG_MAP.get(name);
        if (t == null) {
            t = new Tag(name);
            NAME_TAG_MAP.put(name, t);
        }
        return t;
    }

    public static boolean isValidTagName(String name) {
        return !name.isEmpty() && PATTERN_TAG_NAME.matcher(name).matches();
    }

    private static String checkFixName(String name) {
        name = name.trim().toLowerCase();
        if (name.isEmpty())
            throw new IllegalArgumentException("name must not be empty");
        if (isValidTagName(name))
            return name;
        throw new IllegalArgumentException("name is not valid: " + name);
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

    public ReadOnlyStringProperty nameProperty() {
        return name.getReadOnlyProperty();
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

    public LocalDateTime getDateMod() {
        return dateMod.get();
    }

    public void setDateMod(LocalDateTime dateMod) {
        this.dateMod.set(dateMod);
    }

    public ObjectProperty<LocalDateTime> dateModProperty() {
        return dateMod;
    }

    public long getParentId() {
        return parentId.get();
    }

    public void setParentId(long parentId) {
        this.parentId.set(parentId);
    }

    public LongProperty parentIdProperty() {
        return parentId;
    }

    public long getGroupId() {
        return groupId.get();
    }

    public void setGroupId(long groupId) {
        this.groupId.set(groupId);
    }

    public LongProperty groupIdProperty() {
        return groupId;
    }

//    public void copyTo(Tag other) {
//        other.id.set(this.id.get());
//        other.parentId.set(this.parentId.get());
//        other.groupId.set(this.groupId.get());
//        other.name.set(this.name.get());
//        other.dateAdd.set(this.dateAdd.get());
//        other.dateMod.set(this.dateMod.get());
//    }

    @Override
    public String toString() {
        return name.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tag tag = (Tag) o;

        return name.get().equals(tag.name.get());

    }

    @Override
    public int hashCode() {
        return name.get().hashCode();
    }

    @Override
    public int compareTo(Tag o) {
        return name.get().compareTo(o.name.get());
    }
}
