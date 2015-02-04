/**
    Copyright (C) 2014  AIT / Austrian Institute of Technology
    http://www.ait.ac.at

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see http://www.gnu.org/licenses/agpl-3.0.html
 */
package at.ac.ait.ubicity.rss.impl;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import at.ac.ait.ubicity.commons.broker.BrokerProducer;
import at.ac.ait.ubicity.commons.broker.events.EventEntry;
import at.ac.ait.ubicity.commons.broker.events.EventEntry.Property;
import at.ac.ait.ubicity.commons.broker.exceptions.UbicityBrokerException;
import at.ac.ait.ubicity.commons.cron.AbstractTask;
import at.ac.ait.ubicity.commons.util.PropertyLoader;
import at.ac.ait.ubicity.contracts.rss.RssDTO;

public class RssReaderTask extends AbstractTask {

	final static Logger logger = Logger.getLogger(RssReaderTask.class);

	private RssParser parser;
	private Producer producer;

	private String esIndex;

	class Producer extends BrokerProducer {

		public Producer(PropertyLoader config) throws UbicityBrokerException {
			super.init(config.getString("plugin.rss.broker.user"),
					config.getString("plugin.rss.broker.pwd"));
			setProducer(config.getString("plugin.rss.broker.dest"));
		}
	}

	public RssReaderTask() {
		try {
			PropertyLoader config = new PropertyLoader(
					RssReaderTask.class.getResource("/rss.cfg"));
			esIndex = config.getString("plugin.rss.elasticsearch.index");
			producer = new Producer(config);
		} catch (Exception e) {
			logger.error("Exc. while creating producer", e);
		}
	}

	@Override
	public void executeTask() {

		if (((String) getProperty("URL")).contains("newsbrief")) {
			logger.info("RSS Reader Started: "
					+ (String) getProperty("lastGuid"));
		}

		try {
			parser = new RssParser((String) getProperty("URL"),
					(String) getProperty("lastGuid"));

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

		producer.shutdown();

		if (((String) getProperty("URL")).contains("newsbrief")) {
			logger.info("RSS Reader Stopped: "
					+ (String) getProperty("lastGuid"));
		}
	}

	private EventEntry createEvent(RssDTO data) {
		HashMap<Property, String> header = new HashMap<Property, String>();
		header.put(Property.ES_INDEX, this.esIndex);
		header.put(Property.ES_TYPE, getName());
		header.put(Property.ID, data.getId());

		return new EventEntry(header, data.toJson());
	}

}