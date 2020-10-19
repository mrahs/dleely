/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.exporter;

import pw.ahs.app.dleely.controller.Controller;
import pw.ahs.app.dleely.model.Item;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class TextExporter extends Exporter {

    private TextExportFormatter formatter;
    private final StringBuilder bf;

    public TextExporter(Path path, TextExportFormatter formatter, ExportConfigs exportConfigs) throws IOException {
        super(path, exportConfigs);
        setFormatter(formatter);
        bf = new StringBuilder();

        init();
    }

    public TextExporter(Writer writer, TextExportFormatter formatter, ExportConfigs exportConfigs) throws IOException {
        super(writer, exportConfigs);
        setFormatter(formatter);
        bf = new StringBuilder();

        init();
    }

    public TextExportFormatter getFormatter() {
        return formatter;
    }

    public void setFormatter(TextExportFormatter formatter) {
        this.formatter = Objects.requireNonNull(formatter);
        this.formatter.setConfigs(exportConfigs);
    }

    private void init() throws IOException {
        super.writer.write(formatter.getHeader());
    }

    @Override
    public void put(Item item) throws Exception {
        if (exportConfigs.isSkipAll()) return;
        bf.append(formatter.formatItem(item)).append(formatter.getItemSeparator());
        ++super.count;
    }

    @Override
    public void close() throws Exception {
        if (super.count > 0)
            bf.delete(bf.length() - formatter.getItemSeparator().length(), bf.length());
        super.writer.write(bf.toString());
        super.writer.write(formatter.getFooter());
        super.writer.close();
    }

    public static class TextExportFormatter {
        private ExportConfigs configs;
        private final String header;
        private final String footer;
        private final String itemPattern;
        private final String itemSeparator;
        private final String tagSeparator;
        private final DateTimeFormatter dateTimeFormatter;

        public TextExportFormatter(
                String header,
                String footer,
                String itemPattern,
                String itemSeparator,
                String tagSeparator
        ) {
            this(header, footer, itemPattern, itemSeparator, tagSeparator, null);
        }

        public TextExportFormatter(
                String header,
                String footer,
                String itemPattern,
                String itemSeparator,
                String tagSeparator,
                DateTimeFormatter dateTimeFormatter
        ) {
            this.header = header;
            this.footer = footer;
            this.itemPattern = itemPattern;
            this.itemSeparator = itemSeparator;
            this.tagSeparator = tagSeparator;
            this.dateTimeFormatter = dateTimeFormatter;
        }

        public static TextExportFormatter getDiigoFormatter() {
            String header = "title,url,tags,comments,annotations\n";
            String footer = "";
            String itemPattern = "\"{name}\",\"{ref}\",\"{tags}\",\"{info}\",\"\"";
            String itemSep = "\n";
            String tagSep = " ";

            return new TextExportFormatter(header, footer, itemPattern, itemSep, tagSep);
        }

        public static TextExportFormatter getDleelyCsvFormatter() {
            String header = "name,ref,info,privy,tags,dateadd,datemod\n";
            String footer = "";
            String itemPattern = "\"{name}\",\"{ref}\",\"{info}\",\"{privy}\",\"{tags}\",\"{dateadd}\",\"{datemod}\"";
            String itemSep = "\n";
            String tagSep = " ";

            return new TextExportFormatter(header, footer, itemPattern, itemSep, tagSep);
        }

        public static TextExportFormatter getDleelyJsonFormatter() {
            String header = "[\n";
            String footer = "\n]";
            String itemPattern = "\t{" +
                    "\n\t\"name\":\"{name}\"," +
                    "\n\t\"ref\":\"{ref}\"," +
                    "\n\t\"info\":\"{info}\"," +
                    "\n\t\"privy\":\"{privy}\"," +
                    "\n\t\"tags\":\"{tags}\"," +
                    "\n\t\"dateadd\":\"{dateadd}\"," +
                    "\n\t\"datemod\":\"{datemod}\"," +
                    "\n\t}";
            String itemSep = ",\n";
            String tagSep = " ";

            return new TextExportFormatter(header, footer, itemPattern, itemSep, tagSep);
        }

        public static TextExportFormatter getNetscapeFormatter() {
            String header = "<!DOCTYPE NETSCAPE-Bookmark-file-1>\n" +
                    "<!-- This is an automatically generated file.\n" +
                    "\tIt will be read and overwritten.\n" +
                    "\tDO NOT EDIT! -->\n" +
                    "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n" +
                    "<TITLE>Bookmarks</TITLE>\n" +
                    "<DL><p>\n";
            String footer = "\n</DL><p>\n";
            String itemPattern = "\t<DT><A HREF=\"{ref}\" LAST_VISIT=\"\" ADD_DATE=\"{dateadd}\" LAST_MODIFIED=\"{datemod}\" PRIVATE=\"{privy}\" TAGS=\"{tags}\">{name}</A>\n\t<DD>{info}";
            String itemSep = "\n";
            String tagSep = ",";

            return new TextExportFormatter(header, footer, itemPattern, itemSep, tagSep);
        }

        public ExportConfigs getConfigs() {
            return configs;
        }

        public void setConfigs(ExportConfigs configs) {
            this.configs = Objects.requireNonNull(configs);
        }

        public String getHeader() {
            return header;
        }

        public String getFooter() {
            return footer;
        }

        public String getItemPattern() {
            return itemPattern;
        }

        public String getItemSeparator() {
            return itemSeparator;
        }

        public String getTagSeparator() {
            return tagSeparator;
        }

        public String formatItem(Item item) {
            String i = itemPattern;
            i = i.replace("{id}", (configs.isSkipField(Item.Field.ID) ? "" : "" + item.getId()));
            i = i.replace("{name}", (configs.isSkipField(Item.Field.NAME) ? "" : item.getName()));
            i = i.replace("{ref}", (configs.isSkipField(Item.Field.REF) ? "" : item.getRef()));
            i = i.replace("{info}", (configs.isSkipField(Item.Field.INFO) ? "" : item.getInfo()));
            i = i.replace("{privy}", (configs.isSkipField(Item.Field.PRIVY) ? "" : (item.getPrivy() ? "1" : "0")));
            i = i.replace("{tags}", (configs.isSkipField(Item.Field.TAGS) ? "" : item.getTagsText(tagSeparator)));

            if (configs.isSkipField(Item.Field.DATE_ADD)) {
                i = i.replace("{dateadd}", "");
            } else if (dateTimeFormatter == null) {
                i = i.replace("{dateadd}", "" + Controller.util.toMillis(item.getDateAdd()));
            } else {
                i = i.replace("{dateadd}", dateTimeFormatter.format(item.getDateAdd()));
            }

            if (configs.isSkipField(Item.Field.DATE_MODIFY)) {
                i = i.replace("{datemod}", "");
            } else if (dateTimeFormatter == null) {
                i = i.replace("{datemod}", "" + Controller.util.toMillis(item.getDateMod()));
            } else {
                i = i.replace("{datemod}", dateTimeFormatter.format(item.getDateMod()));
            }

            return i;
        }
    }
}
