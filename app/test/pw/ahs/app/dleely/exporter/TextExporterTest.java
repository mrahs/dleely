/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.exporter;

import org.junit.Before;
import org.junit.Test;
import pw.ahs.app.dleely.Globals;
import pw.ahs.app.dleely.model.Item;
import pw.ahs.app.dleely.model.Tag;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;

public class TextExporterTest {
    private final Collection<Item> items = new ArrayList<>(3);
    @Before
    public void setUp() throws Exception {
        Item tmp = new Item("item 1","ref 1");
        tmp.setInfo("info 1");
        tmp.setPrivy(true);
        tmp.getTags().addAll(Arrays.asList(Tag.getInstance("tag11"), Tag.getInstance("tag12"), Tag.getInstance("tag13")));
        tmp.setDateAdd(LocalDateTime.of(2013, 5, 21, 4, 14));
        tmp.setDateMod(LocalDateTime.of(2013, 5, 22, 4, 14));
        items.add(tmp);

        tmp = new Item("item 2","ref 2");
        tmp.setInfo("info 2");
        tmp.setPrivy(false);
        tmp.getTags().addAll(Arrays.asList(Tag.getInstance("tag21"), Tag.getInstance("tag22"), Tag.getInstance("tag23")));
        tmp.setDateAdd(LocalDateTime.of(2013, 5, 21, 4, 14));
        tmp.setDateMod(LocalDateTime.of(2013, 5, 22, 4, 14));
        items.add(tmp);

        tmp = new Item("item 3","ref 3");
        tmp.setInfo("info 3");
        tmp.setPrivy(false);
        tmp.getTags().addAll(Arrays.asList(Tag.getInstance("tag31"), Tag.getInstance("tag32"), Tag.getInstance("tag33")));
        tmp.setDateAdd(LocalDateTime.of(2013, 5, 21, 4, 14));
        tmp.setDateMod(LocalDateTime.of(2013, 5, 22, 4, 14));
        items.add(tmp);
    }

    @Test
    public void testDiigoExporter() throws Exception {
        StringWriter stringWriter = new StringWriter();
        TextExporter textExporter = new TextExporter(
                stringWriter,
                TextExporter.TextExportFormatter.getDiigoFormatter(),
                Globals.EXPORT_CONFIGS_SKIP_ID_ONLY
        );

        textExporter.putAll(items);
        textExporter.close();

        String res = "title,url,tags,comments,annotations\n" +
                "\"item 1\",\"ref 1\",\"tag11 tag12 tag13\",\"info 1\",\"\"\n"+
                "\"item 2\",\"ref 2\",\"tag21 tag22 tag23\",\"info 2\",\"\"\n"+
                "\"item 3\",\"ref 3\",\"tag31 tag32 tag33\",\"info 3\",\"\"";

        assertEquals(3, textExporter.getExportedCount());
        assertEquals(res, stringWriter.toString());
    }

    @Test
    public void testDleelyCsvExporter() throws Exception {
        StringWriter stringWriter = new StringWriter();
        TextExporter textExporter = new TextExporter(
                stringWriter,
                TextExporter.TextExportFormatter.getDleelyCsvFormatter(),
                Globals.EXPORT_CONFIGS_SKIP_ID_ONLY
        );

        textExporter.putAll(items);
        textExporter.close();

        String res = "name,ref,info,privy,tags,dateadd,datemod\n" +
                "\"item 1\",\"ref 1\",\"info 1\",\"1\",\"tag11 tag12 tag13\",\"1369109640000\",\"1369196040000\"\n"+
                "\"item 2\",\"ref 2\",\"info 2\",\"0\",\"tag21 tag22 tag23\",\"1369109640000\",\"1369196040000\"\n"+
                "\"item 3\",\"ref 3\",\"info 3\",\"0\",\"tag31 tag32 tag33\",\"1369109640000\",\"1369196040000\"";

        assertEquals(3, textExporter.getExportedCount());
        assertEquals(res, stringWriter.toString());
    }

    @Test
    public void testDleelyJsonExporter() throws Exception {
        StringWriter stringWriter = new StringWriter();
        TextExporter textExporter = new TextExporter(
                stringWriter,
                TextExporter.TextExportFormatter.getDleelyJsonFormatter(),
                Globals.EXPORT_CONFIGS_SKIP_ID_ONLY
        );

        textExporter.putAll(items);
        textExporter.close();

        String res = "[\n" +
                "\t{" +
                "\n\t\"name\":\"item 1\"," +
                "\n\t\"ref\":\"ref 1\"," +
                "\n\t\"info\":\"info 1\"," +
                "\n\t\"privy\":\"1\"," +
                "\n\t\"tags\":\"tag11 tag12 tag13\"," +
                "\n\t\"dateadd\":\"1369109640000\"," +
                "\n\t\"datemod\":\"1369196040000\"," +
                "\n\t},\n"+
                "\t{" +
                "\n\t\"name\":\"item 2\"," +
                "\n\t\"ref\":\"ref 2\"," +
                "\n\t\"info\":\"info 2\"," +
                "\n\t\"privy\":\"0\"," +
                "\n\t\"tags\":\"tag21 tag22 tag23\"," +
                "\n\t\"dateadd\":\"1369109640000\"," +
                "\n\t\"datemod\":\"1369196040000\"," +
                "\n\t},\n"+
                "\t{" +
                "\n\t\"name\":\"item 3\"," +
                "\n\t\"ref\":\"ref 3\"," +
                "\n\t\"info\":\"info 3\"," +
                "\n\t\"privy\":\"0\"," +
                "\n\t\"tags\":\"tag31 tag32 tag33\"," +
                "\n\t\"dateadd\":\"1369109640000\"," +
                "\n\t\"datemod\":\"1369196040000\"," +
                "\n\t}"+
                "\n]";

        assertEquals(3, textExporter.getExportedCount());
        assertEquals(res, stringWriter.toString());
    }

    @Test
    public void testNetscapeExporter() throws Exception {
        StringWriter stringWriter = new StringWriter();
        TextExporter textExporter = new TextExporter(
                stringWriter,
                TextExporter.TextExportFormatter.getNetscapeFormatter(),
                Globals.EXPORT_CONFIGS_SKIP_ID_ONLY
        );

        textExporter.putAll(items);
        textExporter.close();

        String res = "<!DOCTYPE NETSCAPE-Bookmark-file-1>\n" +
                "<!-- This is an automatically generated file.\n" +
                "\tIt will be read and overwritten.\n" +
                "\tDO NOT EDIT! -->\n" +
                "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n" +
                "<TITLE>Bookmarks</TITLE>\n" +
                "<DL><p>\n" +
                "\t<DT><A HREF=\"ref 1\" LAST_VISIT=\"\" ADD_DATE=\"1369109640000\" LAST_MODIFIED=\"1369196040000\" PRIVATE=\"1\" TAGS=\"tag11,tag12,tag13\">item 1</A>\n\t<DD>info 1\n"+
                "\t<DT><A HREF=\"ref 2\" LAST_VISIT=\"\" ADD_DATE=\"1369109640000\" LAST_MODIFIED=\"1369196040000\" PRIVATE=\"0\" TAGS=\"tag21,tag22,tag23\">item 2</A>\n\t<DD>info 2\n"+
                "\t<DT><A HREF=\"ref 3\" LAST_VISIT=\"\" ADD_DATE=\"1369109640000\" LAST_MODIFIED=\"1369196040000\" PRIVATE=\"0\" TAGS=\"tag31,tag32,tag33\">item 3</A>\n\t<DD>info 3"+
                "\n</DL><p>\n";

        assertEquals(3, textExporter.getExportedCount());
        assertEquals(res, stringWriter.toString());
    }
}
