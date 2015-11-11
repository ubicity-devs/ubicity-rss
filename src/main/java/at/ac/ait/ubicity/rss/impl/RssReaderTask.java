package at.ac.ait.ubicity.rss.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import at.ac.ait.ubicity.commons.broker.BrokerProducer;
import at.ac.ait.ubicity.commons.broker.events.EventEntry;
import at.ac.ait.ubicity.commons.broker.events.EventEntry.Property;
import at.ac.ait.ubicity.commons.cron.AbstractTask;
import at.ac.ait.ubicity.commons.dto.rss.RssDTO;
import at.ac.ait.ubicity.commons.exceptions.UbicityBrokerException;
import at.ac.ait.ubicity.commons.util.PropertyLoader;

public class RssReaderTask extends AbstractTask {

	private final static Logger logger = Logger.getLogger(RssReaderTask.class);
	private static PropertyLoader config = new PropertyLoader(RssReaderTask.class.getResource("/rss.cfg"));

	private Producer producer;

	private String esIndex;
	private String pluginDest[];

	class Producer extends BrokerProducer {

		public Producer(PropertyLoader config) throws UbicityBrokerException {
			super.init(config.getString("plugin.rss.broker.user"), config.getString("plugin.rss.broker.pwd"));
			pluginDest = config.getStringArray("plugin.rss.broker.dest");
		}
	}

	public RssReaderTask() {
		try {
			esIndex = config.getString("plugin.rss.elasticsearch.index");
			producer = new Producer(config);
		} catch (Exception e) {
			logger.error("Exc. while creating producer", e);
		}
	}

	@Override
	public void executeTask() {

		try {
			RssFetcher rf = new RssFetcher((String) getProperty("URL"));
			rf.start();

			// Wait one minute then interrupt Fetcher thread
			for (int i = 0; i < 60 && rf.isAlive(); i++) {
				Thread.sleep(1000);
			}
			rf.interrupt();
		} catch (Exception e) {
			logger.warn("Caught exc. while fetching updates", e);
		}
	}

	private EventEntry createEvent(RssDTO data) {
		HashMap<Property, String> header = new HashMap<Property, String>();
		header.put(Property.ES_INDEX, this.esIndex);
		header.put(Property.ES_TYPE, getName());
		header.put(Property.ID, data.getId());
		header.put(Property.PLUGIN_CHAIN, EventEntry.formatPluginChain(Arrays.asList(pluginDest)));

		return new EventEntry(header, data.toJson());
	}

	/**
	 * Outsource fetching in Thread to kill it after certain time.
	 * 
	 * @author ruggenthalerc
	 *
	 */
	class RssFetcher extends Thread {

		private final String urlString;

		RssFetcher(String urlString) {
			this.urlString = urlString;
		}

		@Override
		public void run() {
			try {
				RssParser parser = new RssParser(urlString);

				List<RssDTO> dtoList = parser.fetchUpdates();

				dtoList.stream().forEach((dto) -> {
					try {
						EventEntry e = createEvent(dto);

						producer.publish(e);
					} catch (Exception e) {
						logger.warn("Caught exc. while publishing", e);
					}
				});

				if (dtoList.size() > 0) {
					setProperty("lastGuid", dtoList.get(0).getId());
				}

			} catch (Exception e) {
				logger.warn("Caught exc. while fetching updates", e);
			}
		}
	}

}