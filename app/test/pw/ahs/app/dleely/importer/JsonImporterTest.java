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

public class JsonImporterTest {

    @Test
    public void testJsonDleelyImporter() throws Exception {
        StringReader stringReader = new StringReader(
                "[\n" +
                        "\t{" +
                        "\n\t\"name\":\"item 1\"," +
                        "\n\t\"ref\":\"ref 1\"," +
                        "\n\t\"info\":\"info 1\"," +
                        "\n\t\"privy\":\"1\"," +
                        "\n\t\"tags\":\"tag11 tag12 tag13\"," +
                        "\n\t\"dateadd\":\"1343919044665\"," +
                        "\n\t\"datemod\":\"1348613704212\"," +
                        "\n\t},\n" +
                        "\t{" +
                        "\n\t\"name\":\"item 2\"," +
                        "\n\t\"ref\":\"ref 2\"," +
                        "\n\t\"info\":\"info 2\"," +
                        "\n\t\"privy\":\"0\"," +
                        "\n\t\"tags\":\"tag21 tag22 tag23\"," +
                        "\n\t\"dateadd\":\"1343919044665\"," +
                        "\n\t\"datemod\":\"\"," +
                        "\n\t},\n" +
                        "\t{" +
                        "\n\t\"name\":\"\"," +
                        "\n\t\"ref\":\"ref 2\"," +
                        "\n\t\"info\":\"info 2\"," +
                        "\n\t\"privy\":\"0\"," +
                        "\n\t\"tags\":\"tag21 tag22 tag23\"," +
                        "\n\t\"dateadd\":\"1343919044665\"," +
                        "\n\t\"datemod\":\"\"," +
                        "\n\t},\n" +
                        "\t{" +
                        "\n\t\"name\":\"item 2\"," +
                        "\n\t\"ref\":\"\"," +
                        "\n\t\"info\":\"info 2\"," +
                        "\n\t\"privy\":\"0\"," +
                        "\n\t\"tags\":\"tag21 tag22 tag23\"," +
                        "\n\t\"dateadd\":\"1343919044665\"," +
                        "\n\t\"datemod\":\"\"," +
                        "\n\t},\n" +
                        "\t{" +
                        "\n\t\"name\":\"\"," +
                        "\n\t\"ref\":\"\"," +
                        "\n\t\"info\":\"info 2\"," +
                        "\n\t\"privy\":\"0\"," +
                        "\n\t\"tags\":\"tag21 tag22 tag23\"," +
                        "\n\t\"dateadd\":\"1343919044665\"," +
                        "\n\t\"datemod\":\"\"," +
                        "\n\t},\n" +
                        "\t{" +
                        "\n\t\"name\":\"item 3\"," +
                        "\n\t\"ref\":\"ref 3\"," +
                        "\n\t\"info\":\"info 3\"," +
                        "\n\t\"privy\":\"0\"," +
                        "\n\t\"tags\":\"tag31 tag32 tag33\"," +
                        "\n\t\"dateadd\":\"\"," +
                        "\n\t\"datemod\":\"\"," +
                        "\n\t},\n" +
                        "\n]"
        );

        JsonImporter jsonImporter = JsonImporter.getDleelyInstance(stringReader);

        assertTrue(jsonImporter.hasNext());

        Item i = jsonImporter.nextItem();
        assertEquals("item 1", i.getName());
        assertEquals("ref 1", i.getRef());
        assertEquals("info 1", i.getInfo());
        assertTrue(i.getPrivy());
        assertEquals("tag11 tag12 tag13", i.getTagsText(" "));
        assertEquals(Controller.util.toLocalDateTime("1343919044665"), i.getDateAdd());
        assertEquals(Controller.util.toLocalDateTime("1348613704212"), i.getDateMod());

        i = jsonImporter.nextItem();
        assertEquals("item 2", i.getName());
        assertEquals("ref 2", i.getRef());
        assertEquals("info 2", i.getInfo());
        assertFalse(i.getPrivy());
        assertEquals("tag21 tag22 tag23", i.getTagsText(" "));
        assertEquals(Controller.util.toLocalDateTime("1343919044665"), i.getDateAdd());

        i = jsonImporter.nextItem();
        assertEquals("item 3", i.getName());
        assertEquals("ref 3", i.getRef());
        assertEquals("info 3", i.getInfo());
        assertFalse(i.getPrivy());
        assertEquals("tag31 tag32 tag33", i.getTagsText(" "));
    }
}
