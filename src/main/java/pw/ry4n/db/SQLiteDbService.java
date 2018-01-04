package pw.ry4n.db;

import java.util.List;

import edu.uci.ics.crawler4j.crawler.Page;
import pw.ry4n.parser.model.Post;

public interface SQLiteDbService {
	void store(Page webPage);
	void store(Post post);
	void close();
	void recreateSchema();
	List<String> getAllWebpageUrls();
	String getHtmlForUrl(String url);
}
