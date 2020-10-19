/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.importer;

import pw.ahs.app.dleely.controller.Controller;
import pw.ahs.app.dleely.model.Item;
import pw.ahs.app.dleely.model.Tag;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class CsvImporter extends Importer {

    public static CsvImporter getDleelyInstance(Reader reader) throws IOException {
        return new CsvImporter(reader) {
            @Override
            protected void peek() throws IOException {
                String line;
                String[] values = null;
                String name = "";
                String ref = "";
                while ((line = super.reader.readLine()) != null) {
                    if (line.isEmpty()) continue;

                    if ((values = Controller.util.parseCSVLine(line)).length < 7) continue;

                    if ((name = Controller.util.trimQuotes(values[0])).isEmpty()
                            || (ref = Controller.util.trimQuotes(values[1])).isEmpty()) continue;

                    break;
                }

                if (line == null) {
                    super.item = null;
                    return;
                }

                super.item = new Item(name, ref);
                super.item.setInfo(Controller.util.trimQuotes(values[2]));
                super.item.setPrivy(Controller.util.trimQuotes(values[3]).equalsIgnoreCase("1"));

                String[] tags = values[4].split(" ");
                for (String tag : tags) {
                    tag = Controller.util.trimQuotes(tag);
                    if (!Tag.isValidTagName(tag)) continue;
                    super.item.getTags().add(Tag.getInstance(tag));
                }

                String millis = Controller.util.trimQuotes(values[5]);
                if (!millis.isEmpty()) super.item.setDateAdd(Controller.util.toLocalDateTime(millis));
                millis = Controller.util.trimQuotes(values[6]);
                if (!millis.isEmpty()) super.item.setDateMod(Controller.util.toLocalDateTime(millis));
            }
        };
    }

    public static CsvImporter getDleelyInstance(Path path) throws IOException {
        return getDleelyInstance(Files.newBufferedReader(path, Charset.forName("utf-8")));
    }

    public static CsvImporter getDiigoInstance(Reader reader) throws IOException {
        return new CsvImporter(reader) {
            @Override
            protected void peek() throws IOException {
                String line;
                String[] values = null;
                String name = "";
                String ref = "";
                while ((line = super.reader.readLine()) != null) {
                    if (line.isEmpty()) continue;

                    if ((values = Controller.util.parseCSVLine(line)).length < 5) continue;

                    if ((name = Controller.util.trimQuotes(values[0])).isEmpty()
                            || (ref = Controller.util.trimQuotes(values[1])).isEmpty()) continue;

                    break;
                }

                if (line == null) {
                    super.item = null;
                    return;
                }

                super.item = new Item(name, ref);
                super.item.setInfo(Controller.util.trimQuotes(values[3]));

                String[] tags = values[2].split(" ");
                for (String tag : tags) {
                    tag = Controller.util.trimQuotes(tag);
                    if (!Tag.isValidTagName(tag)) continue;
                    super.item.getTags().add(Tag.getInstance(tag));
                }
            }
        };
    }

    public static CsvImporter getDiigoInstance(Path path) throws IOException {
        return getDiigoInstance(Files.newBufferedReader(path, Charset.forName("utf-8")));
    }

    private CsvImporter(Reader reader) throws IOException {
        super(reader);
        peek();
    }

    private CsvImporter(Path path) throws IOException {
        super(path);
        peek();
    }

    protected void peek() throws IOException {
    }

    @Override
    public Item nextItem() throws Exception {
        if (super.item == null) return null;
        Item i = super.item;
        ++super.count;
        peek();
        return i;
    }
}
