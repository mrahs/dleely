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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class XmlImporter extends Importer {

    public static XmlImporter getDleelyInstance(Reader reader) throws XMLStreamException {
        return new XmlImporter(reader) {
            @Override
            protected void peek() throws XMLStreamException {
                String[] data = new String[]{"", "", "", "", "", "", ""};

                while (this.eventReader.hasNext()) {
                    XMLEvent event = this.eventReader.nextEvent();
                    XMLEvent nextEvent = this.eventReader.peek();
                    if (event.isStartElement() && nextEvent.isCharacters()) {
                        String eventName = event.asStartElement().getName().getLocalPart();
                        String nextEventData = nextEvent.asCharacters().getData();
                        switch (eventName) {
                            case "name":
                                data[0] = nextEventData;
                                break;
                            case "ref":
                                data[1] = nextEventData;
                                break;
                            case "info":
                                data[2] = nextEventData;
                                break;
                            case "privy":
                                data[3] = nextEventData;
                                break;
                            case "tags":
                                data[4] = nextEventData;
                                break;
                            case "dateadd":
                                data[5] = nextEventData;
                                break;
                            case "datemod":
                                data[6] = nextEventData;
                                break;
                        }
                    } else if (event.isEndElement()
                            && event.asEndElement().getName().getLocalPart().equalsIgnoreCase("bookmark")) {
                        // lets see if we can build a bookmark
                        if (!data[0].isEmpty() && !data[1].isEmpty()) {
                            // good
                            super.item = new Item(data[0], data[1]);
                            if (!data[2].isEmpty()) super.item.setInfo(data[2]);
                            if (!data[3].isEmpty()) super.item.setPrivy(data[3].equalsIgnoreCase("1"));
                            if (!data[4].isEmpty()) {
                                String[] tags = data[4].split(" ");
                                for (String tag : tags) {
                                    if (!Tag.isValidTagName(tag)) continue;
                                    super.item.getTags().add(Tag.getInstance(tag));
                                }
                            }
                            if (!data[5].isEmpty()) super.item.setDateAdd(Controller.util.toLocalDateTime(data[5]));
                            if (!data[6].isEmpty()) super.item.setDateMod(Controller.util.toLocalDateTime(data[6]));

                            return;
                        }
                        // oops! cannot build a bookmark, keep looping
                        data[0] = "";
                        data[1] = "";
                        data[2] = "";
                        data[3] = "";
                        data[4] = "";
                        data[5] = "";
                        data[6] = "";
                    }
                }
                // end of loop, no more bookmarks
                super.item = null;
            }
        };
    }

    public static XmlImporter getDleelyInstance(Path path) throws IOException, XMLStreamException {
        return getDleelyInstance(Files.newBufferedReader(path, Charset.forName("uft-8")));
    }

    protected final XMLEventReader eventReader;

    private XmlImporter(Reader reader) throws XMLStreamException {
        super(reader);
        this.eventReader = XMLInputFactory.newInstance().createXMLEventReader(super.reader);
        peek();
    }

    private XmlImporter(Path path) throws IOException, XMLStreamException {
        super(path);
        this.eventReader = XMLInputFactory.newInstance().createXMLEventReader(super.reader);
        peek();
    }

    protected void peek() throws XMLStreamException {

    }

    @Override
    public Item nextItem() throws Exception {
        if (super.item == null) return null;
        Item i = super.item;
        ++super.count;
        peek();
        return i;
    }

    @Override
    public void close() throws Exception {
        eventReader.close();
    }
}
