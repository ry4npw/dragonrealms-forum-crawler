package pw.ry4n.db;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import org.junit.Test;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class SQLiteDbServiceTest {
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
			System.out.println("executing store");
			service.store(page);

			// 3. verify insert
			System.out.println("executing select");
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
			System.err.println(e.getMessage());
		}
	}
}
