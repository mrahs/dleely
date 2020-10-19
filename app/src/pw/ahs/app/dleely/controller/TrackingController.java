/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.controller;

import pw.ahs.app.dleely.model.Item;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

public class TrackingController {
    private final Collection<Item> added;
    private final Collection<Item> _added;
    private final Collection<Item> removed;
    private final Collection<Item> _removed;
    private final Collection<Item> edited;
    private final Collection<Item> _edited;

    private static TrackingController instance = null;

    public static TrackingController getInstance() {
        if (instance == null)
            instance = new TrackingController();
        return instance;
    }

    private TrackingController() {
        added = new LinkedHashSet<>();
        _added = Collections.unmodifiableCollection(added);
        removed = new LinkedHashSet<>();
        _removed = Collections.unmodifiableCollection(removed);
        edited = new LinkedHashSet<>();
        _edited = Collections.unmodifiableCollection(edited);
    }

    public void add(Item item) {
        added.add(item);
    }

    public void edit(Item item) {
        if (added.contains(item)) return;
        edited.add(item);
    }

    public void remove(Item item) {
        if (added.remove(item)) return;
        removed.add(item);
        edited.remove(item);
    }

    public void removeAll(Collection<Item> items) {
        for (Item item : items)
            remove(item);
    }

    public void clearAll() {
        added.clear();
        edited.clear();
        removed.clear();
    }

    public boolean contains(Item item) {
        return added.contains(item);
    }

    public Collection<Item> getAdded() {
        return _added;
    }

    public Collection<Item> getRemoved() {
        return _removed;
    }

    public Collection<Item> getEdited() {
        return _edited;
    }
}
