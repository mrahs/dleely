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

public class CsvImporterTest {

    @Test
    public void testDiigoImporter() throws Exception {
        StringReader stringReader = new StringReader(
                "\"item 1\",\"ref 1\",\"tag11 tag12 tag13\",\"info 1\",\"\"\n" +
                        "\"\",\"ref 1\",\"tag11 tag12 tag13\",\"info 1\",\"\"\n" +
                        "\"item 1\",\"\",\"tag11 tag12 tag13\",\"info 1\",\"\"\n" +
                        "\"item 1\",\"ref 1\",\"tag11 tag12 tag13\",\"info 1\"\n" +
                        "\"item 2\",\"ref 2\",\"tag21 tag22 tag23\",\"info 2\",\"\"\n" +
                        "\"item 3\",\"ref 3\",\"tag31 tag32 tag33\",\"info 3\",\"\"\n"
        );
        CsvImporter csvImporter = CsvImporter.getDiigoInstance(stringReader);

        assertTrue(csvImporter.hasNext());

        Item i = csvImporter.nextItem();
        assertEquals("item 1", i.getName());
        assertEquals("ref 1", i.getRef());
        assertEquals("tag11 tag12 tag13", i.getTagsText(" "));
        assertEquals("info 1", i.getInfo());

        i = csvImporter.nextItem();
        assertEquals("item 2", i.getName());
        assertEquals("ref 2", i.getRef());
        assertEquals("tag21 tag22 tag23", i.getTagsText(" "));
        assertEquals("info 2", i.getInfo());

        i = csvImporter.nextItem();
        assertEquals("item 3", i.getName());
        assertEquals("ref 3", i.getRef());
        assertEquals("tag31 tag32 tag33", i.getTagsText(" "));
        assertEquals("info 3", i.getInfo());

        assertFalse(csvImporter.hasNext());
    }


    @Test
    public void testDleelyImporter() throws Exception {
        StringReader stringReader = new StringReader(
                "\"item 1\",\"ref 1\",\"info 1\",\"0\",\"tag11 tag12 tag13\",\"1343919044665\",\"1348613704212\"\n" +
                        "\"item 2\",\"ref 2\",\"info 2\",\"0\",\"tag21 tag22 tag23\",\"1343919044665\",\"\"\n" +
                        "\"\",\"ref 2\",\"info 2\",\"0\",\"tag21 tag22 tag23\",\"1343919044665\",\"\"\n" +
                        "\"item 2\",\"\",\"info 2\",\"0\",\"tag21 tag22 tag23\",\"1343919044665\",\"\"\n" +
                        "\"item 2\",\"ref 2\",\"info 2\",\"tag21 tag22 tag23\",\"1343919044665\",\"\"\n" +
                        "\"item 3\",\"ref 3\",\"info 3\",\"1\",\"tag31 tag32 tag33\",\"\",\"\"\n"
        );
        CsvImporter csvImporter = CsvImporter.getDleelyInstance(stringReader);

        assertTrue(csvImporter.hasNext());

        Item i = csvImporter.nextItem();
        assertEquals("item 1", i.getName());
        assertEquals("ref 1", i.getRef());
        assertEquals("info 1", i.getInfo());
        assertFalse(i.getPrivy());
        assertEquals("tag11 tag12 tag13", i.getTagsText(" "));
        assertEquals(Controller.util.toLocalDateTime("1343919044665"), i.getDateAdd());
        assertEquals(Controller.util.toLocalDateTime("1348613704212"), i.getDateMod());

        i = csvImporter.nextItem();
        assertEquals("item 2", i.getName());
        assertEquals("ref 2", i.getRef());
        assertEquals("info 2", i.getInfo());
        assertFalse(i.getPrivy());
        assertEquals("tag21 tag22 tag23", i.getTagsText(" "));
        assertEquals(Controller.util.toLocalDateTime("1343919044665"), i.getDateAdd());

        i = csvImporter.nextItem();
        assertEquals("item 3", i.getName());
        assertEquals("ref 3", i.getRef());
        assertEquals("info 3", i.getInfo());
        assertTrue(i.getPrivy());
        assertEquals("tag31 tag32 tag33", i.getTagsText(" "));

        assertFalse(csvImporter.hasNext());
    }


}
