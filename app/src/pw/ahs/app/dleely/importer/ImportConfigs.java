/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.importer;

import pw.ahs.app.dleely.model.Item;
import pw.ahs.app.dleely.model.Tag;

import java.time.LocalDateTime;
import java.util.*;

public class ImportConfigs {
    private String defaultName;
    private String defaultInfo;
    private boolean defaultPrivy;
    private final Collection<Tag> tags;

    private boolean skipWhole;
    private final Collection<Tag> collisionTags;
    private final Map<Item.Field, CollisionOption> collisionOptions;

    public ImportConfigs() {
        Item.Field[] fields = Item.Field.values();

        defaultName = "Null";
        defaultInfo = "";
        defaultPrivy = false;
        tags = new LinkedHashSet<>();

        skipWhole = false;
        collisionTags = new LinkedHashSet<>();
        collisionOptions = new HashMap<>(fields.length);
        collisionOptions.put(Item.Field.ID, CollisionOption.SKIP);
        collisionOptions.put(Item.Field.NAME, CollisionOption.MERGE);
        collisionOptions.put(Item.Field.INFO, CollisionOption.MERGE);
        collisionOptions.put(Item.Field.PRIVY, CollisionOption.SKIP);
        collisionOptions.put(Item.Field.TAGS, CollisionOption.MERGE);
        collisionOptions.put(Item.Field.DATE_ADD, CollisionOption.SKIP);
        collisionOptions.put(Item.Field.DATE_MODIFY, CollisionOption.SKIP);
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String name) {
        this.defaultName = name;
    }

    public String getDefaultInfo() {
        return defaultInfo;
    }

    public void setDefaultInfo(String info) {
        this.defaultInfo = info;
    }

    public boolean getDefaultPrivy() {
        return defaultPrivy;
    }

    public void setDefaultPrivy(boolean state) {
        this.defaultPrivy = state;
    }

    public void addTags(Tag... tags) {
        Collections.addAll(this.tags, tags);
    }

    public void removeTags(Tag... tags) {
        for (Tag t : tags) {
            this.tags.remove(t);
        }
    }

    public Collection<Tag> getTags() {
        return Collections.unmodifiableCollection(this.tags);
    }

    public boolean isSkipWhole() {
        return skipWhole;
    }

    public void setSkipWhole(boolean state) {
        this.skipWhole = state;
    }

    public void addCollisionTags(Tag... tags) {
        Collections.addAll(this.collisionTags, tags);
    }

    public void removeCollisionTags(Tag... tags) {
        for (Tag t : tags) {
            this.collisionTags.remove(t);
        }
    }

    public Collection<Tag> getCollisionTags() {
        return Collections.unmodifiableCollection(this.collisionTags);
    }

    public void setCollisionOption(Item.Field f, CollisionOption co) {
        collisionOptions.put(f, co);
    }

    public CollisionOption getCollisionOption(Item.Field f) {
        return collisionOptions.get(f);
    }

    public Item resolve(Item oldItem, Item newItem) {
        // if not equal, return the new one (to be inserted)
        if (!oldItem.equals(newItem)) return newItem;

        if (skipWhole) return oldItem;

        oldItem.getTags().addAll(collisionTags);

        /*
        Refactored to manipulate the new item instead of the old one
        // id is always skipped
        // name
        if (collisionOptions.get(Item.Field.NAME) == CollisionOption.REPLACE)
            oldItem.setName(newItem.getName());
        else if (collisionOptions.get(Item.Field.NAME) == CollisionOption.MERGE)
            oldItem.setName(oldItem.getName() + " | " + newItem.getName());
        // info
        if (collisionOptions.get(Item.Field.INFO) == CollisionOption.REPLACE)
            oldItem.setInfo(newItem.getInfo());
        else if (collisionOptions.get(Item.Field.INFO) == CollisionOption.MERGE)
            oldItem.setInfo(oldItem.getInfo() + "\n" + newItem.getInfo());
        // privy
        if (collisionOptions.get(Item.Field.PRIVY) != CollisionOption.SKIP)
            oldItem.setPrivy(newItem.getPrivy());
        // tags
        if (collisionOptions.get(Item.Field.TAGS) == CollisionOption.REPLACE) {
            oldItem.getTags().clear();
            oldItem.getTags().addAll(newItem.getTags());
        } else if (collisionOptions.get(Item.Field.TAGS) == CollisionOption.MERGE)
            oldItem.getTags().addAll(newItem.getTags());
        // date added
        if (collisionOptions.get(Item.Field.DATE_ADD) != CollisionOption.SKIP)
            oldItem.setDateAdd(newItem.getDateAdd());
        // date modified
        if (collisionOptions.get(Item.Field.DATE_MODIFY) != CollisionOption.SKIP)
            oldItem.setDateMod(newItem.getDateMod());

        return oldItem;
        */

        // id is always skipped
        // name
        if (collisionOptions.get(Item.Field.NAME) == CollisionOption.SKIP)
            newItem.setName(oldItem.getName());
        else if (collisionOptions.get(Item.Field.NAME) == CollisionOption.MERGE)
            newItem.setName(oldItem.getName() + " | " + newItem.getName());
        // info
        if (collisionOptions.get(Item.Field.INFO) == CollisionOption.SKIP)
            newItem.setInfo(oldItem.getInfo());
        else if (collisionOptions.get(Item.Field.INFO) == CollisionOption.MERGE)
            newItem.setInfo(oldItem.getInfo() + "\n" + newItem.getInfo());
        // privy
        if (collisionOptions.get(Item.Field.PRIVY) == CollisionOption.SKIP)
            newItem.setPrivy(oldItem.getPrivy());
        // tags
        if (collisionOptions.get(Item.Field.TAGS) == CollisionOption.SKIP) {
            newItem.getTags().clear();
            newItem.getTags().addAll(oldItem.getTags());
        } else if (collisionOptions.get(Item.Field.TAGS) == CollisionOption.MERGE)
            newItem.getTags().addAll(oldItem.getTags());
        // date added
        if (collisionOptions.get(Item.Field.DATE_ADD) == CollisionOption.SKIP)
            newItem.setDateAdd(oldItem.getDateAdd());
        // date modified
        if (collisionOptions.get(Item.Field.DATE_MODIFY) == CollisionOption.SKIP)
            newItem.setDateMod(oldItem.getDateMod());

        return newItem;
    }

    public void process(Item item) {
        verifyState();

        if (collisionOptions.get(Item.Field.NAME) == CollisionOption.SKIP) item.setName(defaultName);
        if (collisionOptions.get(Item.Field.INFO) == CollisionOption.SKIP
                || item.getInfo().isEmpty()) item.setInfo(defaultInfo);
        if (collisionOptions.get(Item.Field.PRIVY) == CollisionOption.SKIP) item.setPrivy(defaultPrivy);

        if (collisionOptions.get(Item.Field.TAGS) == CollisionOption.SKIP) item.getTags().clear();
        if (collisionOptions.get(Item.Field.DATE_ADD) == CollisionOption.SKIP) item.setDateAdd(LocalDateTime.now());
        if (collisionOptions.get(Item.Field.DATE_MODIFY) == CollisionOption.SKIP) item.setDateMod(LocalDateTime.now());

        item.getTags().addAll(tags);
    }

    private void verifyState() {
        if (collisionOptions.get(Item.Field.NAME) == CollisionOption.SKIP && defaultName.isEmpty())
            throw new IllegalStateException("cannot skip name without a default value");

    }

    public enum CollisionOption {
        SKIP, REPLACE, MERGE
    }
}
