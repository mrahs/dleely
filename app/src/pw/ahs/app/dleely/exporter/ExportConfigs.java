/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.exporter;

import pw.ahs.app.dleely.model.Item;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class ExportConfigs {
    private final Collection<Item.Field> skipFields;

    public ExportConfigs() {
        skipFields = new HashSet<>(8);
    }

    public void addSkipField(Item.Field f) {
        skipFields.add(f);
    }

    public void removeSkipField(Item.Field f) {
        skipFields.remove(f);
    }

    public boolean isSkipField(Item.Field f) {
        return skipFields.contains(f);
    }

    public boolean isSkipAll() {
        return skipFields.containsAll(Arrays.asList(Item.Field.values()));
    }
}
