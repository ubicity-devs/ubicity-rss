package at.ac.ait.ubicity.rss.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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

	private final String lastGuid;

	public RssParser(String urlString, String lastGuid)
			throws MalformedURLException {
		this.url = new URL(urlString);

		this.lastGuid = lastGuid;
	}

	public List<RssDTO> fetchUpdates() throws Exception {

		List<RssDTO> list = new ArrayList<RssDTO>();

		XmlReader reader = null;

		try {
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0");

			reader = new XmlReader(conn);
			SyndFeed feed = new SyndFeedInput().build(reader);

			for (SyndEntry e : feed.getEntries()) {
				if (isNewEntry(e.getUri())) {
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

		} catch (Exception e) {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e1) {
					logger.warn("Exc caught while closing reader", e1);
				}

			logger.warn("Exc caught while loading entries", e);
		}

		logger.info(list.size() + " new RSS Entries read from "
				+ this.url.toString());

		return list;
	}

	private boolean isNewEntry(String curGuid) {

		if (this.lastGuid == null || curGuid == null) {
			return true;
		}
		return this.lastGuid.equals(curGuid);
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