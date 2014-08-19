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

import java.util.ArrayList;
import java.util.List;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.Thread;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.events.Shutdown;

import org.apache.log4j.Logger;

import at.ac.ait.ubicity.commons.cron.AbstractCronPlugin;
import at.ac.ait.ubicity.commons.cron.UbicityCronException;
import at.ac.ait.ubicity.commons.interfaces.CronTask;
import at.ac.ait.ubicity.commons.util.PropertyLoader;
import at.ac.ait.ubicity.rss.RssReader;

@PluginImplementation
public class RssReaderImpl extends AbstractCronPlugin implements RssReader {

	private String name;

	final static Logger logger = Logger.getLogger(RssReaderImpl.class);

	private final List<CronTask> tasks = new ArrayList<CronTask>();

	protected RssReaderImpl() throws UbicityCronException {
		super();
	}

	@Override
	@Init
	public void init() {
		PropertyLoader config = new PropertyLoader(
				RssReaderImpl.class.getResource("/rss.cfg"));
		setPluginConfig(config);

		try {
			RssReaderTask newsbrief = new RssReaderTask();
			newsbrief.setName("newsbrief.eu");
			newsbrief.setTimeInterval("0 0/2 * * * ?");
			newsbrief.setProperty("URL",
					"http://emm.newsbrief.eu/rss?type=rtn&language=en");

			tasks.add(newsbrief);

			initCron(tasks);
		} catch (Exception e) {
			logger.error("Task creation threw error", e);
		}

		logger.info(name + " loaded");
	}

	/**
	 * Sets the Plugin configuration.
	 * 
	 * @param config
	 */
	private void setPluginConfig(PropertyLoader config) {
		this.name = config.getString("plugin.rss.name");
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Thread
	public void run() {

	}

	@Override
	@Shutdown
	public void shutdown() {

	}
}