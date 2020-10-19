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

public class XmlImporterTest {

    @Test
    public void testXmlDleelyImporter() throws Exception {
        StringReader stringReader = new StringReader(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<bookmarks>\n" +
                        "\t<bookmark>\n" +
                        "\t\t<name>item 1</name>\n" +
                        "\t\t<ref>ref 1</ref>\n" +
                        "\t\t<info>info 1</info>\n" +
                        "\t\t<privy>1</privy>\n" +
                        "\t\t<tags>tag11 tag12 tag13</tags>\n" +
                        "\t\t<dateadd>1344011184654</dateadd>\n" +
                        "\t\t<datemod>1348776956940</datemod>\n" +
                        "\t</bookmark>\n" +
                        "\t<bookmark>\n" +
                        "\t\t<name>item 2</name>\n" +
                        "\t\t<ref>ref 2</ref>\n" +
                        "\t\t<info>info 2</info>\n" +
                        "\t\t<privy>0</privy>\n" +
                        "\t\t<tags>tag21 tag22 tag23</tags>\n" +
                        "\t\t<dateadd>1344011184654</dateadd>\n" +
                        "\t</bookmark>\n" +
                        "\t<bookmark>\n" +
                        "\t\t<ref>ref 2</ref>\n" +
                        "\t\t<info>info 2</info>\n" +
                        "\t\t<privy>0</privy>\n" +
                        "\t\t<tags>tag21 tag22 tag23</tags>\n" +
                        "\t\t<dateadd>1344011184654</dateadd>\n" +
                        "\t</bookmark>\n" +
                        "\t<bookmark>\n" +
                        "\t\t<name>item 2</name>\n" +
                        "\t\t<info>info 2</info>\n" +
                        "\t\t<privy>0</privy>\n" +
                        "\t\t<tags>tag21 tag22 tag23</tags>\n" +
                        "\t\t<dateadd>1344011184654</dateadd>\n" +
                        "\t</bookmark>\n" +
                        "\t<bookmark>\n" +
                        "\t\t<name></name>\n" +
                        "\t\t<ref></ref>\n" +
                        "\t\t<info>info 2</info>\n" +
                        "\t\t<privy>0</privy>\n" +
                        "\t\t<tags>tag21 tag22 tag23</tags>\n" +
                        "\t\t<dateadd>1344011184654</dateadd>\n" +
                        "\t</bookmark>\n" +
                        "\t<bookmark>\n" +
                        "\t\t<ref></ref>\n" +
                        "\t\t<info>info 2</info>\n" +
                        "\t\t<privy>0</privy>\n" +
                        "\t\t<tags>tag21 tag22 tag23</tags>\n" +
                        "\t\t<dateadd>1344011184654</dateadd>\n" +
                        "\t</bookmark>\n" +
                        "\t<bookmark>\n" +
                        "\t\t<info>info 3</info>\n" +
                        "\t\t<name>item 3</name>\n" +
                        "\t\t<ref>ref 3</ref>\n" +
                        "\t\t<privy>0</privy>\n" +
                        "\t\t<tags>tag31 tag32 tag33</tags>\n" +
                        "\t</bookmark>\n" +
                        "</bookmarks>\n"
        );

        XmlImporter xmlImporter = XmlImporter.getDleelyInstance(stringReader);

        assertTrue(xmlImporter.hasNext());

        Item i = xmlImporter.nextItem();
        assertEquals("item 1", i.getName());
        assertEquals("ref 1", i.getRef());
        assertEquals("info 1", i.getInfo());
        assertTrue(i.getPrivy());
        assertEquals("tag11 tag12 tag13", i.getTagsText(" "));
        assertEquals(Controller.util.toLocalDateTime("1344011184654"), i.getDateAdd());
        assertEquals(Controller.util.toLocalDateTime("1348776956940"), i.getDateMod());

        i = xmlImporter.nextItem();
        assertEquals("item 2", i.getName());
        assertEquals("ref 2", i.getRef());
        assertEquals("info 2", i.getInfo());
        assertFalse(i.getPrivy());
        assertEquals("tag21 tag22 tag23", i.getTagsText(" "));
        assertEquals(Controller.util.toLocalDateTime("1344011184654"), i.getDateAdd());

        i = xmlImporter.nextItem();
        assertEquals("item 3", i.getName());
        assertEquals("ref 3", i.getRef());
        assertEquals("info 3", i.getInfo());
        assertFalse(i.getPrivy());
        assertEquals("tag31 tag32 tag33", i.getTagsText(" "));
    }
}
