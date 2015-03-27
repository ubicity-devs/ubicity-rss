package at.ac.ait.ubicity.rss.dto;

import java.util.Date;
import java.util.List;

import at.ac.ait.ubicity.commons.templates.AbstractDTO;

import com.google.gson.annotations.SerializedName;

public class RssDTO extends AbstractDTO {

	private String id;

	private String title;

	private String text;
	private String source;

	@SerializedName("created_at")
	private String createdAt = dateAsString(new Date(System.currentTimeMillis()));

	@SerializedName("published_at")
	private String publishedAt;

	private String lang;
	private List<String> categories;

	private String author;

	@SerializedName("geo_point")
	private float[] geoRssPoint;

	public String getId() {
		return this.id;
	}

	public String getTitle() {
		return this.title;
	}

	public String getText() {
		return this.text;
	}

	public String getSource() {
		return this.source;
	}

	public String getCreatedAt() {
		return this.createdAt;
	}

	public String getLang() {
		return this.lang;
	}

	public List<String> getCategories() {
		return this.categories;
	}

	public float[] getGeoRssPoint() {
		return this.geoRssPoint;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public void setGeoRssPoint(float longitude, float latitude) {
		this.geoRssPoint = new float[] { longitude, latitude };
	}

	public void setPublishedAt(Date publishedAt) {
		this.publishedAt = dateAsString(publishedAt);
	}

	public String getPublishedAt() {
		return this.publishedAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = dateAsString(createdAt);
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
}
