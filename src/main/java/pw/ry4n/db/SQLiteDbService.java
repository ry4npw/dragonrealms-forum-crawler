package pw.ry4n.db;

import java.util.Map;

import pw.ry4n.parser.model.Post;

public interface SQLiteDbService {
	/**
	 * Stores a post in the database.
	 * 
	 * @param post
	 */
	void store(Post post);

	/**
	 * Returns a map where the key is the folder and the value is the largest
	 * post_number recorded in the database for that folder.
	 * 
	 * @return a Map<String, Long>
	 */
	public Map<String, Long> getUniqueFolderNamesAndMaxPostNumber();

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
