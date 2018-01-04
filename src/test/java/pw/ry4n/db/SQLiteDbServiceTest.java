package pw.ry4n.db;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import pw.ry4n.parser.model.Post;

public class SQLiteDbServiceTest {
	private static final Logger logger = LoggerFactory.getLogger(SQLiteDbServiceTest.class);

	SQLiteDbServiceImpl service;

	@Test
	public void testCreateDb() throws SQLException {
		service = new SQLiteDbServiceImpl("test.db");

		try {
			HtmlParseData data = new HtmlParseData();

			// 1. create dummy page
			data.setHtml("<html></html>");
			data.setText("text");
			data.setOutgoingUrls(new HashSet<WebURL>());

			WebURL url = new WebURL();
			url.setURL("http://ry4n.pw");

			Page page = new Page(url);
			page.setParseData(data);

			// 2. store dummy page
			logger.debug("executing store");
			service.store(page);

			// 3. verify insert
			logger.debug("executing select");
			Connection connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.
			ResultSet rs = statement.executeQuery("select * from webpage");
			while (rs.next()) {
				assertEquals("<html></html>", rs.getString("html"));
				assertEquals("text", rs.getString("text"));
			}

			// 4. cleanup database
			service.recreateSchema();

			service.close();
		} catch (SQLException e) {
			logger.error("Error in testCreateDb()", e);
		}
	}

	@Test
	public void testInsertDuplicatePost() {
		service = new SQLiteDbServiceImpl("test.db");
		Post post = new Post("folder", 1L, "author", "1", "subject", "body");

		// save duplicate post
		service.store(post);
		service.store(post);

		// cleanup database
		service.recreateSchema();
	}
}
