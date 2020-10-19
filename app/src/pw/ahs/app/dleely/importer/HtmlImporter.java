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
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlImporter extends Importer {
    private static final Pattern NAME_PATTERN = Pattern.compile("(?i)<A[^>]*>([^<]*)</A>");
    private static final Pattern REF_PATTERN = Pattern.compile("(?i)HREF=\"([^\"]*)\"");
    private static final Pattern INFO_PATTERN = Pattern.compile("(?i)<DD>([^\n]*)");
    private static final Pattern PRIVY_PATTERN = Pattern.compile("(?i)PRIVATE=\"(\\d)\"");
    private static final Pattern TAGS_PATTERN = Pattern.compile("(?i)TAGS=\"([^\"]*)\"");
    private static final Pattern DATE_ADD_PATTERN = Pattern.compile("(?i)ADD_DATE=\"([^\"]*)\"");
    private static final Pattern DATE_MODIFY_PATTERN = Pattern.compile("(?i)LAST_MODIFIED=\"([^\"]*)\"");
    private static final Pattern LINK_PATTERN = Pattern.compile("(?i)<\\s*a\\s*href=[\"']([^\"']*)[\"'][^>]*>([^<]*)<\\s*/\\s*a\\s*>");

    public static HtmlImporter getNetscapeInstance(Reader reader) throws IOException {
        return new HtmlImporter(reader) {
            @Override
            protected void peek() throws IOException {
                String line;
                if (this.lastLine == null)
                    line = super.reader.readLine();
                else {
                    line = this.lastLine;
                    this.lastLine = null;
                }
                String name = "";
                String ref = "";
                Matcher nameMatcher;
                Matcher refMatcher;
                do {
                    if (line.isEmpty()) continue;

                    nameMatcher = NAME_PATTERN.matcher(line);
                    refMatcher = REF_PATTERN.matcher(line);

                    if (!nameMatcher.find() || !refMatcher.find()) continue;

                    name = nameMatcher.group(1);
                    ref = refMatcher.group(1);

                    if (name.isEmpty() || ref.isEmpty()) continue;

                    break;
                } while ((line = super.reader.readLine()) != null);

                if (line == null) {
                    super.item = null;
                    return;
                }

                super.item = new Item(name, ref);

                Matcher matcher;

                matcher = PRIVY_PATTERN.matcher(line);
                if (matcher.find()) super.item.setPrivy(matcher.group(1).equalsIgnoreCase("1"));

                matcher = TAGS_PATTERN.matcher(line);
                if (matcher.find()) {
                    String[] tags = matcher.group(1).split(",");
                    for (String tag : tags) {
                        if (!Tag.isValidTagName(tag)) continue;
                        super.item.getTags().add(Tag.getInstance(tag));
                    }
                }

                matcher = DATE_ADD_PATTERN.matcher(line);
                if (matcher.find() && !matcher.group(1).isEmpty())
                    super.item.setDateAdd(Controller.util.toLocalDateTime(matcher.group(1)));

                matcher = DATE_MODIFY_PATTERN.matcher(line);
                LocalDateTime dateMod = null;
                if (matcher.find() && !matcher.group(1).isEmpty())
                    dateMod = Controller.util.toLocalDateTime(matcher.group(1));

                line = super.reader.readLine();
                if (line == null) return;
                matcher = INFO_PATTERN.matcher(line);
                if (matcher.find()) super.item.setInfo(matcher.group(1));
                else this.lastLine = line;

                if (dateMod != null) super.item.setDateMod(dateMod);
            }
        };
    }

    public static HtmlImporter getNetscapeInstance(Path path) throws IOException {
        return getNetscapeInstance(Files.newBufferedReader(path, Charset.forName("utf-8")));
    }

    protected String lastLine;
    private int lastLineIndex;

    public HtmlImporter(Reader reader) throws IOException {
        super(reader);
        init();
    }

    public HtmlImporter(Path path) throws IOException {
        super(path);
        init();
    }

    private void init() throws IOException {
        this.lastLine = null;
        this.lastLineIndex = 0;
        peek();
    }

    protected void peek() throws IOException {
        if (lastLine == null)
            newPeek();
        else
            nextPeek();
    }

    private void newPeek() throws IOException {
        String line;
        String name = "";
        String ref = "";
        Matcher matcher;
        while ((line = super.reader.readLine()) != null) {
            if (line.isEmpty()) continue;

            matcher = LINK_PATTERN.matcher(line);

            while (matcher.find()) {
                name = matcher.group(2);
                ref = matcher.group(1);
                if (name.isEmpty() || ref.isEmpty()) continue;
                break;
            }

            if (name.isEmpty() || ref.isEmpty()) continue;

            lastLineIndex = matcher.end();
            lastLine = line;

            break;
        }


        if (line == null) {
            super.item = null;
            return;
        }

        super.item = new Item(name, ref);
    }

    private void nextPeek() throws IOException {
        Matcher matcher = LINK_PATTERN.matcher(lastLine);
        if (matcher.find(lastLineIndex)) {
            super.item = new Item(matcher.group(2), matcher.group(1));
            lastLineIndex = matcher.end();
        } else {
            lastLine = null;
            lastLineIndex = 0;
            newPeek();
        }
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
