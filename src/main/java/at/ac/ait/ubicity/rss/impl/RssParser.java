package at.ac.ait.ubicity.rss.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import at.ac.ait.ubicity.rss.dto.RssDTO;

import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

public class RssParser {

	final static Logger logger = Logger.getLogger(RssParser.class);

	private final URL url;

	public RssParser(String urlString, String lastGuid) throws MalformedURLException {
		this.url = new URL(urlString);
	}

	public List<RssDTO> fetchUpdates() throws Exception {

		List<RssDTO> list = new ArrayList<RssDTO>();

		try {
			SyndFeedInput input = new SyndFeedInput();
			input.setXmlHealerOn(true);
			SyndFeed feed = input.build(new XmlReader(url.openStream()));

			for (SyndEntry e : feed.getEntries()) {
				RssDTO dto = new RssDTO();
				dto.setId(e.getUri());
				dto.setTitle(e.getTitle());

				if (e.getDescription() != null) {
					dto.setText(Jsoup.clean(e.getDescription().getValue(), Whitelist.simpleText()));
				}

				dto.setSource(e.getLink());
				dto.setPublishedAt(e.getPublishedDate());
				dto.setAuthor(e.getAuthor());

				List<String> cats = new ArrayList<String>();
				for (SyndCategory cat : e.getCategories()) {
					if (cat != null) {
						cats.add(cat.getName());
					}
				}

				dto.setCategories(cats);

				dto.setLang(readForeignMarkup(e.getForeignMarkup(), ForeignRssTag.LANG));

				String geo = readForeignMarkup(e.getForeignMarkup(), ForeignRssTag.GEO_POINT);

				if (geo != null) {
					String[] geoAr = geo.split(" ");

					if (geoAr.length == 2) {
						dto.setGeoRssPoint(Float.parseFloat(geoAr[1]), Float.parseFloat(geoAr[0]));
					}
				}

				list.add(dto);
			}
		} catch (Exception e) {
			logger.warn("Exc caught while loading entries", e);
		}

		logger.info(list.size() + " new RSS Entries read from " + this.url.toString());

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