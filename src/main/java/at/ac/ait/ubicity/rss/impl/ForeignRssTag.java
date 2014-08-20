package at.ac.ait.ubicity.rss.impl;

public enum ForeignRssTag {
	LANG("language"), //
	GEO_POINT("point");

	private String name;

	ForeignRssTag(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}