/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.importer;

import org.junit.Test;
import pw.ahs.app.dleely.controller.Controller;
import pw.ahs.app.dleely.model.Item;

import java.io.StringReader;

import static junit.framework.Assert.*;

public class HtmlImporterTest {

    @Test
    public void testNetscapeImporter() throws Exception {
        StringReader stringReader = new StringReader(
                "<!DOCTYPE NETSCAPE-Bookmark-file-1>\n" +
                        "<!-- This is an automatically generated file.\n" +
                        "\tIt will be read and overwritten.\n" +
                        "\tDO NOT EDIT! -->\n" +
                        "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n" +
                        "<TITLE>Bookmarks</TITLE>\n" +
                        "<DL><p>\n" +
                        "\t\t<DT><A HREF=\"ref 1\" LAST_VISIT=\"\" ADD_DATE=\"1343919044665\" LAST_MODIFIED=\"1348613704212\" PRIVATE=\"0\" TAGS=\"tag11,tag12,tag13\">item 1</A>\n" +
                        "\t\t<DD>info 1\n" +
                        "\t\t<DT><A HREF=\"\" LAST_VISIT=\"\" ADD_DATE=\"\" PRIVATE=\"0\" TAGS=\"tag21,tag22,tag23\">item 2</A>\n" +
                        "\t\t<DD>info 2\n" +
                        "\t\t<DT><A HREF=\"ref 2\" LAST_VISIT=\"\" ADD_DATE=\"\" PRIVATE=\"0\" TAGS=\"tag21,tag22,tag23\"></A>\n" +
                        "\t\t<DD>info 2\n" +
                        "\t\t<DT><A HREF=\"ref 2\" LAST_VISIT=\"\" ADD_DATE=\"\" PRIVATE=\"0\" TAGS=\"tag21,tag22,tag23\">item 2</A>\n" +
                        "\t\t<DD>info 2\n" +
                        "\t\t<DT><A HREF=\"ref 3\" LAST_VISIT=\"\" ADD_DATE=\"\" PRIVATE=\"1\" TAGS=\"tag31,tag32,tag33\">item 3</A>\n" +
                        "\t\t<DT><A HREF=\"ref 4\" LAST_VISIT=\"\" ADD_DATE=\"\" PRIVATE=\"1\" TAGS=\"tag41,tag32,tag33\">item 4</A>\n" +
                        "</DL><p>\n"
        );

        HtmlImporter htmlImporter = HtmlImporter.getNetscapeInstance(stringReader);

        assertTrue(htmlImporter.hasNext());

        Item i = htmlImporter.nextItem();
        assertEquals("item 1", i.getName());
        assertEquals("ref 1", i.getRef());
        assertEquals("info 1", i.getInfo());
        assertFalse(i.getPrivy());
        assertEquals("tag11 tag12 tag13", i.getTagsText(" "));
        assertEquals(Controller.util.toLocalDateTime("1343919044665"), i.getDateAdd());
        assertEquals(Controller.util.toLocalDateTime("1348613704212"), i.getDateMod());

        i = htmlImporter.nextItem();
        assertEquals("item 2", i.getName());
        assertEquals("ref 2", i.getRef());
        assertEquals("info 2", i.getInfo());
        assertFalse(i.getPrivy());
        assertEquals("tag21 tag22 tag23", i.getTagsText(" "));

        i = htmlImporter.nextItem();
        assertEquals("item 3", i.getName());
        assertEquals("ref 3", i.getRef());
        assertEquals("", i.getInfo());
        assertTrue(i.getPrivy());
        assertEquals("tag31 tag32 tag33", i.getTagsText(" "));

        i = htmlImporter.nextItem();
        assertEquals("item 4", i.getName());
        assertEquals("ref 4", i.getRef());
        assertEquals("", i.getInfo());
        assertTrue(i.getPrivy());
        assertEquals("tag32 tag33 tag41", i.getTagsText(" "));
    }

    @Test
    public void testHtmlImporter() throws Exception {
        StringReader stringReader = new StringReader(
                "<a href=\"ref 1\">item 1</a>" +
                        "<a href=\"ref 2\">item 2</a>" +
                        "\n" +
                        " this is some text \n" +
                        "bla bla <a href=\"ref 3\">item 3</a> hi there <a href=\"ref 4\">item 4</a>" +
                        "<a href=\"ref 5\">item 5</a> hi there <a href=\"ref 6\">item 6</a> bla bla <a href=\"ref 7\">item 7</a>"
        );

        HtmlImporter htmlImporter = new HtmlImporter(stringReader);

        assertTrue(htmlImporter.hasNext());

        Item i = htmlImporter.nextItem();
        assertEquals("item 1", i.getName());
        assertEquals("ref 1", i.getRef());

        i = htmlImporter.nextItem();
        assertEquals("item 2", i.getName());
        assertEquals("ref 2", i.getRef());

        i = htmlImporter.nextItem();
        assertEquals("item 3", i.getName());
        assertEquals("ref 3", i.getRef());

        i = htmlImporter.nextItem();
        assertEquals("item 4", i.getName());
        assertEquals("ref 4", i.getRef());

        i = htmlImporter.nextItem();
        assertEquals("item 5", i.getName());
        assertEquals("ref 5", i.getRef());

        i = htmlImporter.nextItem();
        assertEquals("item 6", i.getName());
        assertEquals("ref 6", i.getRef());

        i = htmlImporter.nextItem();
        assertEquals("item 7", i.getName());
        assertEquals("ref 7", i.getRef());

        assertFalse(htmlImporter.hasNext());
    }
}
