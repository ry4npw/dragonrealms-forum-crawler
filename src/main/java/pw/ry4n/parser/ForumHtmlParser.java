package pw.ry4n.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pw.ry4n.db.SQLiteDbService;
import pw.ry4n.db.SQLiteDbServiceImpl;
import pw.ry4n.parser.model.Post;

/**
 * An HTML parser for forum.play.net posts. This parser will:
 * 
 * <ol>
 * <li>Parse the "folder" for the current page.</li>
 * <li>Parse out each post_message entry</li>
 * <li>Persist each post using the {@link SQLiteDbService}</li>
 * </ol>
 * 
 * @author Ryan Powell
 */
public class ForumHtmlParser {
	private static final Logger logger = LoggerFactory.getLogger(ForumHtmlParser.class);
	private SQLiteDbService service = new SQLiteDbServiceImpl("forum.db");

	/**
	 * Parse the HTML for forum posts and persist them.
	 * 
	 * @param html
	 */
	public void parsePost(String html) {
		// use Jsoup to parse HTML
		Document doc = Jsoup.parse(html);

		// parse the "folder" for the current page
		Elements folderTree = doc.select(".content .parenting_tree");

		// if there is no parenting_tree on the page, then there are no posts
		if (folderTree.size() > 0) {
			String folderString = getFolder(folderTree);

			// parse posts
			Elements postElements = doc.select(".post_master_container");
			logger.debug("saving " + postElements.size() + " posts on page.");
			for (Element postElement : postElements) {
				// persist each post
				service.store(createPost(folderString, postElement));
			}
		}
	}

	/**
	 * Creates the folder string, e.g. "DragonRealms/Lore/Smithing Skill" based
	 * on the current page.
	 * 
	 * @param folderTree
	 *            the Jsoup Elements selected with ".content .parenting_tree"
	 * @return the folder string
	 */
	private String getFolder(Elements folderTree) {
		StringBuilder folder = new StringBuilder();

		// combine elements with a "/"
		Elements folderElements = folderTree.first().select("a");

		for (Element folderElement : folderElements) {
			// skip "All Forums"
			if ("All Forums".equals(folderElement.text())) {
				continue;
			}

			if (folder.length() > 0) {
				// separate folders with a "/"
				folder.append("/");
			}

			// the forums treat a category "/" as a backtick
			folder.append(folderElement.text().replace("/", "`"));
		}

		return folder.toString();
	}

	private Post createPost(String folderString, Element postElement) {
		return new Post(folderString, Long.parseLong(postElement.select(".post_message_post_id").text()),
				postElement.select(".post_author_name").text(), postElement.select(".post_time").text().substring(3),
				postElement.select(".post_subject").text(), postElement.select(".post_content_body").text());
	}
}
