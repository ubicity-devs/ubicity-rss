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
import at.ac.ait.ubicity.commons.util.ESIndexCreator;
import at.ac.ait.ubicity.commons.util.PropertyLoader;

public class RssReaderTask extends AbstractTask {

	private final static Logger logger = Logger.getLogger(RssReaderTask.class);
	private static PropertyLoader config = new PropertyLoader(RssReaderTask.class.getResource("/rss.cfg"));

	private static Producer producer;

	private ESIndexCreator ic;
	private static String pluginDest[];

	class Producer extends BrokerProducer {

		public Producer(PropertyLoader config) throws UbicityBrokerException {
			super.init();
			pluginDest = config.getStringArray("plugin.rss.broker.dest");
		}
	}

	public RssReaderTask() {
		try {
			ic = new ESIndexCreator(config.getString("plugin.rss.elasticsearch.index"), "", config.getString("plugin.rss.elasticsearch.pattern"));
			if (producer == null)
				producer = new Producer(config);
		} catch (Exception e) {
			logger.error("Exc. while creating producer", e);
		}
	}

	@Override
	public void executeTask() {

		try {
			RssParser parser = new RssParser((String) getProperty("URL"));

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

	private EventEntry createEvent(RssDTO data) {
		HashMap<Property, String> header = new HashMap<Property, String>();
		header.put(Property.ES_INDEX, ic.getIndex());
		header.put(Property.ES_TYPE, getName());
		header.put(Property.ID, data.getId());
		header.put(Property.PLUGIN_CHAIN, EventEntry.formatPluginChain(Arrays.asList(pluginDest)));

		return new EventEntry(header, data.toJson());
	}
}