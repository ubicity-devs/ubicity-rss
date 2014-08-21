package at.ac.ait.ubicity.rss.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom2.Element;

import at.ac.ait.ubicity.contracts.rss.RssDTO;

import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

public class RssParser {

	final static Logger logger = Logger.getLogger(RssParser.class);

	private final URL url;
	private static SyndFeedInput input = new SyndFeedInput();

	private String lastStoredId = "";

	public RssParser(String urlString, String lastStoredId)
			throws MalformedURLException {
		this.url = new URL(urlString);
		this.lastStoredId = lastStoredId;
	}

	public List<RssDTO> fetchUpdates() throws Exception {

		List<RssDTO> list = new ArrayList<RssDTO>();
		SyndFeed feed = input.build(new XmlReader(url));

		for (SyndEntry e : feed.getEntries()) {
			if (lastStoredId != null && lastStoredId.equals(e.getUri())) {
				break;
			} else {
				RssDTO dto = new RssDTO();
				dto.setId(e.getUri());
				dto.setTitle(e.getTitle());
				dto.setText(e.getDescription().getValue());
				dto.setSource(e.getLink());
				dto.setCreatedAt(e.getPublishedDate());

				List<String> cats = new ArrayList<String>();
				for (SyndCategory cat : e.getCategories()) {
					cats.add(cat.getName());
				}

				dto.setCategories(cats);

				dto.setLang(readForeignMarkup(e.getForeignMarkup(),
						ForeignRssTag.LANG));

				String geo = readForeignMarkup(e.getForeignMarkup(),
						ForeignRssTag.GEO_POINT);

				if (geo != null) {
					String[] geoAr = geo.split(" ");
					dto.setGeoRssPoint(Float.parseFloat(geoAr[1]),
							Float.parseFloat(geoAr[0]));
				}

				list.add(dto);
			}
		}

		logger.info(list.size() + " new RSS Entries read from "
				+ this.url.toString());

		return list;
	}

	private String readForeignMarkup(List<Element> list, ForeignRssTag tag) {

		for (Element e : list) {
			if (tag.getName().equalsIgnoreCase(e.getName())) {
				return e.getContent(0).getValue();
			}
		}

		return null;
	}
}