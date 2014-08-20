package at.ac.ait.ubicity.rss.impl;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;

import at.ac.ait.ubicity.contracts.rss.RssDTO;

public class RssParser {

	final static Logger logger = Logger.getLogger(RssParser.class);

	private final URL url;
	private static XMLInputFactory factory = XMLInputFactory.newInstance();

	private String lastStoredId = "";

	public RssParser(String urlString, String lastStoredId)
			throws MalformedURLException {
		this.url = new URL(urlString);
		this.lastStoredId = lastStoredId;
	}

	public List<RssDTO> fetchUpdates() throws Exception {

		List<RssDTO> list = new ArrayList<RssDTO>();

		InputStream in = url.openStream();
		XMLEventReader eventReader = factory.createXMLEventReader(in);
		boolean alreadyProcessed = false;

		while (eventReader.hasNext() && !alreadyProcessed) {
			XMLEvent event = eventReader.nextEvent();

			if (event.isStartElement()) {
				String localPart = event.asStartElement().getName()
						.getLocalPart();

				if (RssTag.ITEM.getName().equalsIgnoreCase(localPart)) {

					RssDTO dto = setContent(eventReader);
					if (lastStoredId != null
							&& lastStoredId.equals(dto.getId())) {
						alreadyProcessed = true;
					} else {
						list.add(dto);
					}
				}
			}
		}

		in.close();
		logger.info(list.size() + " new RSS Entries read from "
				+ this.url.toString());

		return list;
	}

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

					SimpleDateFormat format = new SimpleDateFormat(
							"EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
					String date = getElementData(event, eventReader);
					try {
						dto.setCreatedAt(format.parse(date));
					} catch (ParseException e) {
						logger.warn("Not able to parse date: " + date);
					}
				} else if (RssTag.LANG.getName().equalsIgnoreCase(localPart)) {
					dto.setLang(getElementData(event, eventReader));
				} else if (RssTag.CATEGORY.getName()
						.equalsIgnoreCase(localPart)) {
					dto.setCategory(getElementData(event, eventReader));
				} else if (RssTag.GEO_POINT.getName().equalsIgnoreCase(
						localPart)) {
					String[] geoPoint = getElementData(event, eventReader)
							.split(" ");

					dto.setGeoRssPoint(Float.parseFloat(geoPoint[1]),
							Float.parseFloat(geoPoint[0]));
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