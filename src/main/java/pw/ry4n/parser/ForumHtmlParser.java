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

public class ForumHtmlParser {
	private static final Logger logger = LoggerFactory.getLogger(ForumHtmlParser.class);
	private SQLiteDbService service = new SQLiteDbServiceImpl("forum.db");

	public void parsePost(String html) {
		Document doc = Jsoup.parse(html);
		StringBuilder folder = new StringBuilder();
		Elements folderTree = doc.select(".content .parenting_tree");
		if (folderTree.size() > 0) {
			Elements folderElements = folderTree.first().select("a");
			boolean firstElement = true;
			for (Element folderElement : folderElements) {
				if (firstElement) {
					firstElement = false;
				} else {
					folder.append(" / ");
				}
				folder.append(folderElement.text());
			}
			Elements postElements = doc.select(".post_master_container");
			logger.debug("saving " + postElements.size() + " posts on page.");
			for (Element postElement : postElements) {
				service.store(new Post(folder.toString(),
						Long.parseLong(postElement.select(".post_message_post_id").text()),
						postElement.select(".post_author_name").text(),
						postElement.select(".post_time").text().substring(3),
						postElement.select(".post_subject").text(),
						postElement.select(".post_content_body").text()));
			}
		}
	}
}
