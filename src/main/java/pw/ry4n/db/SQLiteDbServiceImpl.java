package pw.ry4n.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang.Validate;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;

public class SQLiteDbServiceImpl implements SQLiteDbService {
	Connection connection;
	String databaseName;
	PreparedStatement insertKeyStatement;

	public SQLiteDbServiceImpl() {
		this("sqlite.db");
	}

	public SQLiteDbServiceImpl(String databaseName) {
		try {
			createDatabase(databaseName);
		} catch (SQLException e) {
			// create db failed.
			System.err.println(e);
		}
	}

	private void createDatabase(String databaseName) throws SQLException {
		Validate.notEmpty(databaseName);
		this.databaseName = databaseName;
		connection = DriverManager.getConnection("jdbc:sqlite:" + databaseName);

		connection.createStatement()
				.executeUpdate("CREATE TABLE IF NOT EXISTS webpage ( "
						+ "  id integer primary key autoincrement,"
						+ "  html text," + "  text text,"
						+ "  url varchar(4096),"
						+ "  seen DATETIME DEFAULT CURRENT_TIMESTAMP" + ")");

		insertKeyStatement = connection.prepareStatement("insert into webpage(html, text, url) values(?,?,?)");
	}

	public void store(Page page) {
		if (page.getParseData() instanceof HtmlParseData) {
			try {

				HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();

				insertKeyStatement.setString(1, htmlParseData.getHtml());
				insertKeyStatement.setString(2, htmlParseData.getText());
				insertKeyStatement.setString(3, page.getWebURL().getURL());
				insertKeyStatement.executeUpdate();
			} catch (SQLException e) {
				System.err.println(e);
			}
		}
	}

	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e);
			}
		}
	}

	public void recreateSchema() {
		try {
			connection.createStatement().executeUpdate("DROP TABLE IF EXISTS webpage");
			createDatabase(this.databaseName);
		} catch (SQLException e) {
			System.err.println(e);
		}
	}
}
