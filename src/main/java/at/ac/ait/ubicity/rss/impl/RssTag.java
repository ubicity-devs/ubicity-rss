package at.ac.ait.ubicity.rss.impl;

public enum RssTag {
	ITEM("item"), //

	ID("guid"), //
	TITLE("title"), //
	TEXT("description"), //
	SOURCE("link"), //
	CREATED_AT("pubDate"), //
	LANG("language"), //
	CATEGORY("category"), //
	GEO_POINT("point");

	private String name;

	RssTag(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}