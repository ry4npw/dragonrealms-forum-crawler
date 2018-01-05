package pw.ry4n.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		connection.createStatement()
				.executeUpdate("CREATE TABLE IF NOT EXISTS post ( " + "  id integer primary key autoincrement,"
						+ "  folder text NOT NULL," + "  post_number varchar(10) NOT NULL," + "  author text,"
						+ "  time text," + "  subject text," + "  body text,"
						+ "  CONSTRAINT uc_post UNIQUE (post_number, folder)" + ")");

		insertPostStatement = connection.prepareStatement(
				"insert into post(folder, post_number, author, time, subject, body) values(?, ?, ?, ?, ?, ?)");
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
			logger.error("duplicate post: " + post);
			try {
				insertPostStatement.close();
				insertPostStatement = connection.prepareStatement(
						"insert into post(folder, post_number, author, time, subject, body) values(?, ?, ?, ?, ?, ?)");
			} catch (SQLException e2) {
				logger.error("could not recreate statement.");
			}
		}
	}

	void recreateSchema() {
		try {
			connection.createStatement().executeUpdate("DROP TABLE IF EXISTS post");
			createDatabase(this.databaseName);
		} catch (SQLException e) {
			logger.error("Error recreating schema", e);
		}
	}

	Connection getConnection() {
		return connection;
	}
}
