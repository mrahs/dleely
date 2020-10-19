/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.exporter;

import pw.ahs.app.dleely.model.Item;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public abstract class Exporter implements AutoCloseable {
    protected final BufferedWriter writer;
    protected ExportConfigs exportConfigs;
    protected long count;

    public Exporter(Writer writer, ExportConfigs exportConfigs) {
        if (writer instanceof BufferedWriter)
            this.writer = (BufferedWriter) writer;
        else
            this.writer = new BufferedWriter(writer);
        this.exportConfigs = exportConfigs;
        this.count = 0;
    }

    protected Exporter(Path path, ExportConfigs exportConfigs) throws IOException {
        this(Files.newBufferedWriter(path, Charset.forName("utf-8")), exportConfigs);
    }

    public Writer getWriter() {
        return writer;
    }

    public abstract void put(Item item) throws Exception;

    public long putAll(Collection<Item> items) throws Exception {
        if (exportConfigs.isSkipAll()) return count;
        for (Item i : items) put(i);
        return count;
    }

    public ExportConfigs getConfigs() {
        return exportConfigs;
    }

    public void setConfigs(ExportConfigs configs) {
        this.exportConfigs = configs;
    }

    public long getExportedCount() {
        return count;
    }

    @Override
    public void close() throws Exception {
        writer.close();
    }
}
