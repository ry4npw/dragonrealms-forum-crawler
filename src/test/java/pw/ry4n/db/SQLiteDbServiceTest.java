package pw.ry4n.db;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pw.ry4n.parser.model.Post;

public class SQLiteDbServiceTest {
	protected static final Logger logger = LoggerFactory.getLogger(SQLiteDbServiceTest.class);

	private static final String DATABASE_NAME = "test.db";

	private SQLiteDbServiceImpl service;

	/**
	 * Test service.store(Post).
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testInsertPost() throws SQLException {
		if (service == null) {
			service = new SQLiteDbServiceImpl(DATABASE_NAME);
		}

		// save post
		Post post = new Post("folder", 1L, "author", "01/04/2017 9:13 AM CDT", "subject", "body");
		service.store(post);

		// try again (should fail silently)
		service.store(post);

		verifyRowCount("post", 1L);

		// cleanup database
		service.recreateSchema();
		service.close();
	}

	/**
	 * Assert that the table has the specified number of rows.
	 * 
	 * @param table
	 *            the table name
	 * @param expectedRowCount
	 *            expected row count
	 * @throws SQLException
	 */
	private void verifyRowCount(String table, long expectedRowCount) throws SQLException {
		Connection connection = service.getConnection();
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(30); // set timeout to 30 sec.

		// select count
		ResultSet rs = statement.executeQuery("select count(*) as rows from " + table);
		while (rs.next()) {
			// assert count equals expectedCount
			assertEquals(expectedRowCount, rs.getLong("rows"));
		}
	}
}
