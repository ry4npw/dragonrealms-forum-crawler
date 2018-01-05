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

		// @formatter:off
		connection.createStatement()
				.executeUpdate("CREATE TABLE IF NOT EXISTS post ("
						+ "folder text,"
						+ "post_number varchar(10),"
						+ "author varchar(20),"
						+ "time text,"
						+ "subject text,"
						+ "body text,"
						+ "CONSTRAINT us_forumdata UNIQUE (folder, post_number)" + ")");
		// @formatter:on

		prepareInsertPost();
	}

	private void prepareInsertPost() throws SQLException {
		insertPostStatement = connection.prepareStatement(
				"insert into post (folder, post_number, author, time, subject, body) values (?, ?, ?, ?, ?, ?)");
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
			logger.warn("duplicate post: " + post);
			try {
				insertPostStatement.close();
				prepareInsertPost();
			} catch (SQLException e2) {
				logger.error("could not recreate statement.");
			}
		}
	}

	/**
	 * Package private method to refresh the schema between unit tests.
	 */
	void recreateSchema() {
		try {
			connection.createStatement().executeUpdate("DROP TABLE IF EXISTS forumdata");
			connection.createStatement().executeUpdate("DROP TABLE IF EXISTS post");
			createDatabase(this.databaseName);
		} catch (SQLException e) {
			logger.error("Error recreating schema", e);
		}
	}

	/**
	 * Package private method to return the current connection for unit tests.
	 * 
	 * @return the database connection
	 */
	Connection getConnection() {
		return connection;
	}

	public void createFTS() {
		try {
			// drop any existing table
			connection.createStatement().executeUpdate("DROP TABLE IF EXISTS forumdata");

			// create FTS table
			connection.createStatement().executeUpdate(
					"CREATE VIRTUAL TABLE IF NOT EXISTS forumdata (folder, post_number, author, time, subject, body)");

			// copy data
			connection.createStatement().executeUpdate(
					"INSERT INTO forumdata SELECT (folder, post_number, author, time, subject, body) FROM post");
			connection.createStatement().executeUpdate("DROP TABLE post");

			// reduce disk size
			connection.createStatement().executeUpdate("VACUUM");
		} catch (SQLException e) {
			logger.error("Error creating FTS for post table.", e);
		}
	}
}
