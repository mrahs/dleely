/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.importer;

import pw.ahs.app.dleely.model.Item;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public abstract class Importer implements AutoCloseable {

    protected final BufferedReader reader;
    protected Item item;
    protected long count;

    public Importer(Reader reader) {
        if (reader instanceof BufferedReader)
            this.reader = (BufferedReader) reader;
        else
            this.reader = new BufferedReader(reader);
        this.item = null;
        this.count = 0;
    }

    public Importer(Path path) throws IOException {
        this(Files.newBufferedReader(path, Charset.forName("UTF-8")));
    }

    public Reader getReader() {
        return reader;
    }

    public abstract Item nextItem() throws Exception;

    public boolean hasNext() {
        return item != null;
    }

    public Collection<Item> getAllItems() throws Exception {
        if (!hasNext()) return null;

        Collection<Item> items = new ArrayList<>();
        while (hasNext()) {
            items.add(nextItem());
        }
        return items;
    }

    public long loadAllItems(Collection<Item> items) throws Exception {
        if (!hasNext() || items == null) return count;

        while (hasNext()) {
            items.add(nextItem());
        }
        return count;
    }

    public long getImportedCount() {
        return count;
    }

    @Override
    public void close() throws Exception {
        reader.close();
    }
}
