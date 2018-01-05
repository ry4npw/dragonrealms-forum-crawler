package pw.ry4n.db;

import pw.ry4n.parser.model.Post;

public interface SQLiteDbService {
	void store(Post post);
	void close();
}
