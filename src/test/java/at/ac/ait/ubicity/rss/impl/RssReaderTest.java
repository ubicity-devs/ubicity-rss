package at.ac.ait.ubicity.rss.impl;

import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

public class RssReaderTest {

	@Ignore
	@Test
	public void test() throws Exception {

		URL feedUrl = new URL(
				"http://emm.newsbrief.eu/rss?type=rtn&language=en");

		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = input.build(new XmlReader(feedUrl));

		System.out.println(feed);
	}
}
