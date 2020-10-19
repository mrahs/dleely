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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonImporter extends Importer {
    private static final Pattern NAME_PATTERN = Pattern.compile("(?i)\"name\":\"([^\"]*)\"");
    private static final Pattern REF_PATTERN = Pattern.compile("(?i)\"ref\":\"([^\"]*)\"");
    private static final Pattern INFO_PATTERN = Pattern.compile("(?i)\"info\":\"([^\"]*)\"");
    private static final Pattern PRIVY_PATTERN = Pattern.compile("(?i)\"privy\":\"([^\"]*)\"");
    private static final Pattern TAGS_PATTERN = Pattern.compile("(?i)\"tags\":\"([^\"]*)\"");
    private static final Pattern DATE_ADD_PATTERN = Pattern.compile("(?i)\"dateadd\":\"([^\"]*)\"");
    private static final Pattern DATE_MODIFY_PATTERN = Pattern.compile("(?i)\"datemod\":\"([^\"]*)\"");

    public static JsonImporter getDleelyInstance(Reader reader) throws IOException {
        return new JsonImporter(reader) {
            @Override
            protected void peek() throws IOException {
                String line;
                Matcher nameMatcher;
                Matcher refMatcher;
                String name;
                String ref;
                String values;
                while (true) {
                    while ((line = super.reader.readLine()) != null) {
                        if (line.contains("{")) break;
                    }
                    if (line == null) {
                        super.item = null;
                        return;
                    }

                    StringBuilder obj = new StringBuilder(line);
                    while ((line = super.reader.readLine()) != null) {
                        obj.append(line);
                        if (line.contains("}")) break;
                    }
                    if (line == null) {
                        super.item = null;
                        return;
                    }

                    values = obj.toString().replaceAll("[\\t\\n\\r]", "");
                    nameMatcher = NAME_PATTERN.matcher(values);
                    refMatcher = REF_PATTERN.matcher(values);
                    if (!nameMatcher.find() || !refMatcher.find()) continue;

                    name = nameMatcher.group(1);
                    ref = refMatcher.group(1);
                    if (!name.isEmpty() && !ref.isEmpty()) break;
                }

                super.item = new Item(name, ref);

                Matcher matcher = INFO_PATTERN.matcher(values);
                if (matcher.find()) super.item.setInfo(matcher.group(1));

                matcher = PRIVY_PATTERN.matcher(values);
                if (matcher.find()) super.item.setPrivy(matcher.group(1).equalsIgnoreCase("1"));

                matcher = TAGS_PATTERN.matcher(values);
                if (matcher.find()) {
                    String[] tags = matcher.group(1).split(" ");
                    for (String tag : tags) {
                        if (!Tag.isValidTagName(tag)) continue;
                        super.item.getTags().add(Tag.getInstance(tag));
                    }
                }

                matcher = DATE_ADD_PATTERN.matcher(values);
                if (matcher.find() && !matcher.group(1).isEmpty())
                    super.item.setDateAdd(Controller.util.toLocalDateTime(matcher.group(1)));

                matcher = DATE_MODIFY_PATTERN.matcher(values);
                if (matcher.find() && !matcher.group(1).isEmpty())
                    super.item.setDateMod(Controller.util.toLocalDateTime(matcher.group(1)));
            }
        };
    }

    public static JsonImporter getDleelyInstance(Path path) throws IOException {
        return getDleelyInstance(Files.newBufferedReader(path, Charset.forName("uft-8")));
    }

    private JsonImporter(Reader reader) throws IOException {
        super(reader);
        peek();
    }

    private JsonImporter(Path path) throws IOException {
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
