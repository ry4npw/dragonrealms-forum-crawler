package pw.ry4n.parser.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Forum Post POJO.
 */
public class Post {
	private String folder;
	private long postNumber;
	private String author;
	private String time;
	private String subject;
	private String body;

	public Post() {
		// default constructor
	}

	public Post(String folder, long postNumber, String author, String time, String subject, String body) {
		this.folder = folder;
		this.postNumber = postNumber;
		this.author = author;
		this.time = time;
		this.subject = subject;
		this.body = body;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public long getPostNumber() {
		return postNumber;
	}

	public void setPostNumber(long postNumber) {
		this.postNumber = postNumber;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
