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

public class XMLExporterTest {
    private final Collection<Item> items = new ArrayList<>(3);

    @Before
    public void setUp() throws Exception {
        Item tmp = new Item("item 1", "ref 1");
        tmp.setInfo("info 1");
        tmp.setPrivy(true);
        tmp.getTags().addAll(Arrays.asList(Tag.getInstance("tag11"), Tag.getInstance("tag12"), Tag.getInstance("tag13")));
        tmp.setDateAdd(LocalDateTime.of(2013, 5, 21, 4, 14));
        tmp.setDateMod(LocalDateTime.of(2013, 5, 22, 4, 14));
        items.add(tmp);

        tmp = new Item("item 2", "ref 2");
        tmp.setInfo("info 2");
        tmp.setPrivy(false);
        tmp.getTags().addAll(Arrays.asList(Tag.getInstance("tag21"), Tag.getInstance("tag22"), Tag.getInstance("tag23")));
        tmp.setDateAdd(LocalDateTime.of(2013, 5, 21, 4, 14));
        tmp.setDateMod(LocalDateTime.of(2013, 5, 22, 4, 14));
        items.add(tmp);

        tmp = new Item("item 3", "ref 3");
        tmp.setInfo("info 3");
        tmp.setPrivy(false);
        tmp.getTags().addAll(Arrays.asList(Tag.getInstance("tag31"), Tag.getInstance("tag32"), Tag.getInstance("tag33")));
        tmp.setDateAdd(LocalDateTime.of(2013, 5, 21, 4, 14));
        tmp.setDateMod(LocalDateTime.of(2013, 5, 22, 4, 14));
        items.add(tmp);
    }

    @Test
    public void testXmlDleelyExporter() throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLExporter xmlExporter = XMLExporter.getDleelyExporter(stringWriter, Globals.EXPORT_CONFIGS_SKIP_ID_ONLY);

        xmlExporter.putAll(items);
        xmlExporter.close();

        String res =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<bookmarks>\n" +
                        "\t<bookmark>\n" +
                        "\t\t<name>item 1</name>\n" +
                        "\t\t<ref>ref 1</ref>\n" +
                        "\t\t<info>info 1</info>\n" +
                        "\t\t<privy>1</privy>\n" +
                        "\t\t<tags>tag11 tag12 tag13</tags>\n" +
                        "\t\t<dateadd>1369109640000</dateadd>\n" +
                        "\t\t<datemod>1369196040000</datemod>\n" +
                        "\t</bookmark>\n" +
                        "\t<bookmark>\n" +
                        "\t\t<name>item 2</name>\n" +
                        "\t\t<ref>ref 2</ref>\n" +
                        "\t\t<info>info 2</info>\n" +
                        "\t\t<privy>0</privy>\n" +
                        "\t\t<tags>tag21 tag22 tag23</tags>\n" +
                        "\t\t<dateadd>1369109640000</dateadd>\n" +
                        "\t\t<datemod>1369196040000</datemod>\n" +
                        "\t</bookmark>\n" +
                        "\t<bookmark>\n" +
                        "\t\t<name>item 3</name>\n" +
                        "\t\t<ref>ref 3</ref>\n" +
                        "\t\t<info>info 3</info>\n" +
                        "\t\t<privy>0</privy>\n" +
                        "\t\t<tags>tag31 tag32 tag33</tags>\n" +
                        "\t\t<dateadd>1369109640000</dateadd>\n" +
                        "\t\t<datemod>1369196040000</datemod>\n" +
                        "\t</bookmark>\n" +
                        "</bookmarks>";

        assertEquals(3, xmlExporter.getExportedCount());
        assertEquals(res, stringWriter.toString());
    }
}