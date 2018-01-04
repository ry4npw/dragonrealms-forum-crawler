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
	PreparedStatement deleteWebpageByUrl;

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
				+ "  folder text NOT NULL,"
				+ "  post_number varchar(10) NOT NULL,"
				+ "  author text,"
				+ "  time text,"
				+ "  subject text,"
				+ "  body text,"
				+ "  CONSTRAINT uc_post UNIQUE (post_number, folder)"
				+ ")");

		insertWebpageStatement = connection.prepareStatement("insert into webpage(html, text, url) values(?,?,?)");
		insertPostStatement = connection.prepareStatement("insert into post(folder, post_number, author, time, subject, body) values(?, ?, ?, ?, ?, ?)");
		selectHtmlForUrlStatement = connection.prepareStatement("select html from webpage where url=?");
		deleteWebpageByUrl = connection.prepareStatement("delete from webpage where url=?");
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
			insertPostStatement.setString(1, post.getFolder());
			insertPostStatement.setLong(2, post.getPostNumber());
			insertPostStatement.setString(3, post.getAuthor());
			insertPostStatement.setString(4, post.getTime());
			insertPostStatement.setString(5, post.getSubject());
			insertPostStatement.setString(6, post.getBody());
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
	public List<String> getHtmlForUrl(String url) {
		List<String> htmls = new ArrayList<>();

		try {
			selectHtmlForUrlStatement.setString(1, url);
			ResultSet rs = selectHtmlForUrlStatement.executeQuery();
			while (rs.next()) {
				htmls.add(rs.getString("html"));
			}
		} catch (SQLException e) {
			logger.error("Error in getHtmlForUrl()", e);
		}

		return htmls;
	}

	@Override
	public void deleteWebpageByUrl(String url) {
		try {
			deleteWebpageByUrl.setString(1, url);
			deleteWebpageByUrl.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error in deleteWebpageByUrl()", e);
		}
	}
}
