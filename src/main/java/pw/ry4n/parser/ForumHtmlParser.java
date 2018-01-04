package pw.ry4n.parser;

import java.util.ArrayList;
import java.util.List;

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
	private static SQLiteDbService service = new SQLiteDbServiceImpl("forum.db");

	public static void main(String[] args) throws Exception {
		for (String url : service.getAllWebpageUrls()) {
			parsePost(url);
			service.deleteWebpageByUrl(url);
		}

		service.close();
	}

	private static List<Post> parsePost(String url) {
		List<Post> posts = new ArrayList<>();

		List<String> htmls = service.getHtmlForUrl(url);
		for (String html : htmls) {
			Document doc = Jsoup.parse(html);
			StringBuilder folder = new StringBuilder();
			Elements folderElements = doc.select(".content .parenting_tree").first().select("a");
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
			logger.debug("found " + postElements.size() + " posts on page.");
			for (Element postElement : postElements) {
				Post post = new Post(folder.toString(), Long.parseLong(postElement.select(".post_message_post_id").text()), postElement.select(".post_author_name").text(), postElement.select(".post_time").text().substring(3), postElement.select(".post_subject").text(),
						postElement.select(".post_content_body").text());
				logger.debug("saving " + post);
				service.store(post);
			}
		}

		return posts;
	}
}
