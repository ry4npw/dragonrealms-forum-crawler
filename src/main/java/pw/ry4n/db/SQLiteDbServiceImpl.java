package pw.ry4n.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import pw.ry4n.parser.model.Post;

public class SQLiteDbServiceImpl implements SQLiteDbService {
	private static final Logger logger = LoggerFactory.getLogger(SQLiteDbServiceImpl.class);

	Connection connection;
	String databaseName;
	PreparedStatement insertWebpageStatement;
	PreparedStatement selectHtmlForUrlStatement;
	PreparedStatement insertPostStatement;

	public SQLiteDbServiceImpl() {
		this("sqlite.db");
	}

	public SQLiteDbServiceImpl(String databaseName) {
		try {
			createDatabase(databaseName);
		} catch (SQLException e) {
			// create db failed.
			logger.error("Error in SQLiteDbServiceImpl constructor.", e);
		}
	}

	private void createDatabase(String databaseName) throws SQLException {
		Validate.notEmpty(databaseName);
		this.databaseName = databaseName;
		connection = DriverManager.getConnection("jdbc:sqlite:" + databaseName);

		connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS webpage ( "
						+ "  id integer primary key autoincrement,"
						+ "  html text," + "  text text,"
						+ "  url varchar(4096),"
						+ "  seen DATETIME DEFAULT CURRENT_TIMESTAMP" + ")");

		connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS post ( "
				+ "  id integer primary key autoincrement,"
				+ "  author text,"
				+ "  folder text,"
				+ "  post_number varchar(10),"
				+ "  time text,"
				+ "  subject text,"
				+ "  body text,"
				+ "  CONSTRAINT uc1 UNIQUE (post_number, folder)"
				+ ")");

		insertWebpageStatement = connection.prepareStatement("insert into webpage(html, text, url) values(?,?,?)");
		insertPostStatement = connection.prepareStatement("insert into post(author, time, subject, body) values(?, ?, ?, ?)");
		selectHtmlForUrlStatement = connection.prepareStatement("select html from webpage where url=?");
	}

	public void store(Page page) {
		if (page.getParseData() instanceof HtmlParseData) {
			try {

				HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();

				insertWebpageStatement.setString(1, htmlParseData.getHtml());
				insertWebpageStatement.setString(2, htmlParseData.getText());
				insertWebpageStatement.setString(3, page.getWebURL().getURL());
				insertWebpageStatement.executeUpdate();
			} catch (SQLException e) {
				logger.error("Error in store()", e);
			}
		}
	}

	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				// connection close failed.
				logger.error("Error closing JDBC connection.", e);
			}
		}
	}

	public void recreateSchema() {
		try {
			connection.createStatement().executeUpdate("DROP TABLE IF EXISTS post");
			connection.createStatement().executeUpdate("DROP TABLE IF EXISTS webpage");
			createDatabase(this.databaseName);
		} catch (SQLException e) {
			logger.error("Error recreating schema", e);
		}
	}

	public void store(Post post) {
		try {
			insertPostStatement.setString(1, post.getAuthor());
			insertPostStatement.setString(2, post.getTime());
			insertPostStatement.setString(3, post.getSubject());
			insertPostStatement.setString(4, post.getBody());
			insertPostStatement.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error in store()", e);
		}
	}

	public List<String> getAllWebpageUrls() {
		List<String> urls = new ArrayList<>();

		try {
			ResultSet rs = connection.createStatement().executeQuery("select url from webpage");
			while (rs.next()) {
				urls.add(rs.getString("url"));
			}
		} catch (SQLException e) {
			logger.error("Error in getAllWebpageUrls()", e);
		}

		return urls;
	}

	@Override
	public String getHtmlForUrl(String url) {
		try {
			selectHtmlForUrlStatement.setString(1, url);
			ResultSet rs = selectHtmlForUrlStatement.executeQuery();
			if (rs.next()) {
				return rs.getString("html");
			}
		} catch (SQLException e) {
			logger.error("Error in getHtmlForUrl()", e);
		}
		return null;
	}
}
