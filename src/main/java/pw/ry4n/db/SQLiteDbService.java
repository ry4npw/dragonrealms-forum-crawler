package pw.ry4n.db;

import pw.ry4n.parser.model.Post;

public interface SQLiteDbService {
	/**
	 * Stores a post in the database.
	 * 
	 * @param post
	 */
	void store(Post post);

	/**
	 * Closes the database connection. This should be called when finished with
	 * the service.
	 */
	void close();

	/**
	 * Creates a Full Text Search virtual table called "forumdata" and populates
	 * it from data in the post table.
	 */
	void createFTS();
}
