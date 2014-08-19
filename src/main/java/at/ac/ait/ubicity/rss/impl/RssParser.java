package at.ac.ait.ubicity.rss.impl;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import at.ac.ait.ubicity.contracts.rss.RssDTO;

public class RssParser {

	private final URL url;
	private static XMLInputFactory factory = XMLInputFactory.newInstance();

	private String lastStoredId;

	public RssParser(String urlString) throws MalformedURLException {
		this.url = new URL(urlString);
	}

	public List<RssDTO> fetchUpdates() throws Exception {

		List<RssDTO> list = new ArrayList<RssDTO>();

		InputStream in = url.openStream();
		XMLEventReader eventReader = factory.createXMLEventReader(in);

		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();

			if (event.isStartElement()) {
				String localPart = event.asStartElement().getName()
						.getLocalPart();

				if (RssTag.ITEM.getName().equalsIgnoreCase(localPart)) {
					RssDTO dto = setContent(eventReader);
					list.add(dto);

					if (lastStoredId.equals(dto.getId())) {
						return list;
					}
				}
			}
		}

		return list;
	}

	@SuppressWarnings("deprecation")
	private RssDTO setContent(XMLEventReader eventReader)
			throws XMLStreamException {

		RssDTO dto = new RssDTO();

		// build dto unless end item tag reached
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();

			// return is closing tag is reached
			if (event.isEndElement()) {
				String localPart = event.asEndElement().getName()
						.getLocalPart();

				if (RssTag.ITEM.getName().equalsIgnoreCase(localPart)) {
					return dto;
				}
			}

			if (event.isStartElement()) {
				String localPart = event.asStartElement().getName()
						.getLocalPart();

				if (RssTag.ID.getName().equalsIgnoreCase(localPart)) {
					dto.setId(getElementData(event, eventReader));
				} else if (RssTag.TITLE.getName().equalsIgnoreCase(localPart)) {
					dto.setTitle(getElementData(event, eventReader));
				} else if (RssTag.TEXT.getName().equalsIgnoreCase(localPart)) {
					dto.setText(getElementData(event, eventReader));
				} else if (RssTag.SOURCE.getName().equalsIgnoreCase(localPart)) {
					dto.setSource(getElementData(event, eventReader));
				} else if (RssTag.CREATED_AT.getName().equalsIgnoreCase(
						localPart)) {
					dto.setCreatedAt(new Date(
							getElementData(event, eventReader)));
				} else if (RssTag.LANG.getName().equalsIgnoreCase(localPart)) {
					dto.setLang(getElementData(event, eventReader));
				} else if (RssTag.CATEGORY.getName()
						.equalsIgnoreCase(localPart)) {
					dto.setCategory(getElementData(event, eventReader));
				} else if (RssTag.GEO_POINT.getName().equalsIgnoreCase(
						localPart)) {
					String[] geoPoint = getElementData(event, eventReader)
							.split(" ");

					dto.setGeoRssPoint(Float.parseFloat(geoPoint[0]),
							Float.parseFloat(geoPoint[1]));
				}

			}
		}

		return dto;
	}

	private String getElementData(XMLEvent event, XMLEventReader eventReader)
			throws XMLStreamException {
		String result = "";
		event = eventReader.nextEvent();
		if (event instanceof Characters) {
			result = event.asCharacters().getData();
		}
		return result;
	}
}