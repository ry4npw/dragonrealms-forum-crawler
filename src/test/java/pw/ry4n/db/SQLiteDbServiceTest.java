package pw.ry4n.db;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pw.ry4n.parser.model.Post;

public class SQLiteDbServiceTest {
	private static final Logger logger = LoggerFactory.getLogger(SQLiteDbServiceTest.class);

	private static final String DATABASE_NAME = "test.db";

	private SQLiteDbServiceImpl service;

	@Test
	public void testInsertDuplicatePost() {
		try {
			// test data
			service = new SQLiteDbServiceImpl(DATABASE_NAME);
			Post post = new Post("folder", 1L, "author", "1", "subject", "body");

			// save duplicate post
			service.store(post);
			service.store(post);

			// check row count on database
			Connection connection = service.getConnection();
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.
			ResultSet rs = statement.executeQuery("select count(*) as rows from post");
			while (rs.next()) {
				assertEquals(1L, rs.getLong("rows"));
			}

			// cleanup database
			service.recreateSchema();
			service.close();
		} catch (SQLException e) {
			logger.error("Error in testInsertDuplicatePost()", e);
		}
	}
}
