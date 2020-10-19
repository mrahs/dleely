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

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class XMLExporter extends Exporter {

    public static XMLExporter getDleelyExporter(Path path, ExportConfigs exportConfigs) throws IOException, XMLStreamException {
        return getDleelyExporter(Files.newBufferedWriter(path, Charset.forName("UTF-8")), exportConfigs);
    }

    public static XMLExporter getDleelyExporter(Writer writer, ExportConfigs exportConfigs) throws XMLStreamException {
        return new XMLExporter(writer, exportConfigs) {
            @Override
            protected void init() throws XMLStreamException {
                this.eventWriter.add(this.eventFactory.createStartDocument("UTF-8", "1.0", true));
                this.eventWriter.add(this.endLine);

                this.eventWriter.add(this.eventFactory.createStartElement("", "", "bookmarks"));
                this.eventWriter.add(this.endLine);
            }

            @Override
            public void put(Item item) throws Exception {
                if (super.exportConfigs.isSkipAll()) return;

                this.eventWriter.add(this.tab);
                this.eventWriter.add(this.eventFactory.createStartElement("", "", "bookmark"));
                this.eventWriter.add(this.endLine);

                if (!super.exportConfigs.isSkipField(Item.Field.ID)) {
                    this.eventWriter.add(this.tab);
                    this.eventWriter.add(this.tab);
                    this.eventWriter.add(this.eventFactory.createStartElement("", "", "id"));
                    this.eventWriter.add(this.eventFactory.createCharacters("" + item.getId()));
                    this.eventWriter.add(this.eventFactory.createEndElement("", "", "id"));
                    this.eventWriter.add(this.endLine);
                }

                if (!super.exportConfigs.isSkipField(Item.Field.NAME)) {
                    this.eventWriter.add(this.tab);
                    this.eventWriter.add(this.tab);
                    this.eventWriter.add(this.eventFactory.createStartElement("", "", "name"));
                    this.eventWriter.add(this.eventFactory.createCharacters(item.getName()));
                    this.eventWriter.add(this.eventFactory.createEndElement("", "", "name"));
                    this.eventWriter.add(this.endLine);
                }

                if (!super.exportConfigs.isSkipField(Item.Field.REF)) {
                    this.eventWriter.add(this.tab);
                    this.eventWriter.add(this.tab);
                    this.eventWriter.add(this.eventFactory.createStartElement("", "", "ref"));
                    this.eventWriter.add(this.eventFactory.createCharacters(item.getRef()));
                    this.eventWriter.add(this.eventFactory.createEndElement("", "", "ref"));
                    this.eventWriter.add(this.endLine);
                }

                if (!super.exportConfigs.isSkipField(Item.Field.NAME) && !item.getInfo().isEmpty()) {
                    this.eventWriter.add(this.tab);
                    this.eventWriter.add(this.tab);
                    this.eventWriter.add(this.eventFactory.createStartElement("", "", "info"));
                    this.eventWriter.add(this.eventFactory.createCharacters(item.getInfo()));
                    this.eventWriter.add(this.eventFactory.createEndElement("", "", "info"));
                    this.eventWriter.add(this.endLine);
                }

                if (!super.exportConfigs.isSkipField(Item.Field.PRIVY)) {
                    this.eventWriter.add(this.tab);
                    this.eventWriter.add(this.tab);
                    this.eventWriter.add(this.eventFactory.createStartElement("", "", "privy"));
                    this.eventWriter.add(this.eventFactory.createCharacters((item.getPrivy() ? "1" : "0")));
                    this.eventWriter.add(this.eventFactory.createEndElement("", "", "privy"));
                    this.eventWriter.add(this.endLine);
                }
                if (!super.exportConfigs.isSkipField(Item.Field.TAGS)) {
                    String tags = item.getTagsText(" ");
                    if (!tags.isEmpty()) {
                        this.eventWriter.add(this.tab);
                        this.eventWriter.add(this.tab);
                        this.eventWriter.add(this.eventFactory.createStartElement("", "", "tags"));
                        this.eventWriter.add(this.eventFactory.createCharacters(tags));
                        this.eventWriter.add(this.eventFactory.createEndElement("", "", "tags"));
                        this.eventWriter.add(this.endLine);
                    }
                }

                if (!super.exportConfigs.isSkipField(Item.Field.DATE_ADD)) {
                    this.eventWriter.add(this.tab);
                    this.eventWriter.add(this.tab);
                    this.eventWriter.add(this.eventFactory.createStartElement("", "", "dateadd"));
                    this.eventWriter.add(this.eventFactory.createCharacters("" + Controller.util.toMillis(item.getDateAdd())));
                    this.eventWriter.add(this.eventFactory.createEndElement("", "", "dateadd"));
                    this.eventWriter.add(this.endLine);
                }

                if (!super.exportConfigs.isSkipField(Item.Field.DATE_MODIFY)) {
                    this.eventWriter.add(this.tab);
                    this.eventWriter.add(this.tab);
                    this.eventWriter.add(this.eventFactory.createStartElement("", "", "datemod"));
                    this.eventWriter.add(this.eventFactory.createCharacters("" + Controller.util.toMillis(item.getDateMod())));
                    this.eventWriter.add(this.eventFactory.createEndElement("", "", "datemod"));
                    this.eventWriter.add(this.endLine);
                }

                this.eventWriter.add(this.tab);
                this.eventWriter.add(this.eventFactory.createEndElement("", "", "bookmark"));
                this.eventWriter.add(this.endLine);

                ++super.count;
            }

            @Override
            public void close() throws Exception {
                this.eventWriter.add(this.eventFactory.createEndElement("", "", "bookmarks"));
                this.eventWriter.add(this.eventFactory.createEndDocument());
                this.eventWriter.close();
            }
        };
    }

    protected final XMLEventWriter eventWriter;
    protected final XMLEventFactory eventFactory;
    protected final XMLEvent endLine;
    protected final XMLEvent tab;

    protected XMLExporter(Path path, ExportConfigs exportConfigs) throws IOException, XMLStreamException {
        super(path, exportConfigs);
        this.eventWriter = XMLOutputFactory.newInstance().createXMLEventWriter(super.writer);
        this.eventFactory = XMLEventFactory.newFactory();
        this.endLine = eventFactory.createDTD("\n");
        this.tab = eventFactory.createDTD("\t");
    }

    protected XMLExporter(Writer writer, ExportConfigs exportConfigs) throws XMLStreamException {
        super(writer, exportConfigs);
        this.eventWriter = XMLOutputFactory.newInstance().createXMLEventWriter(super.writer);
        this.eventFactory = XMLEventFactory.newFactory();
        this.endLine = eventFactory.createDTD("\n");
        this.tab = eventFactory.createDTD("\t");
        init();
    }

    protected void init() throws XMLStreamException {

    }

    @Override
    public void put(Item item) throws Exception {

    }

    @Override
    public void close() throws Exception {
        eventWriter.close();
    }
}
