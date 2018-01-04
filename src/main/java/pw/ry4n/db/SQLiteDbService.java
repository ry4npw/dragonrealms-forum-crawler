package pw.ry4n.db;

import edu.uci.ics.crawler4j.crawler.Page;

public interface SQLiteDbService {
	void store(Page webPage);
	void close();
	void recreateSchema();
}
