package pw.ry4n.crawler;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import pw.ry4n.parser.ForumHtmlParser;

public class ForumCrawler extends WebCrawler {
	private static final Logger logger = LoggerFactory.getLogger(ForumCrawler.class);

	private static final Pattern FILTER = Pattern.compile("\\/view\\/\\d+(\\?force_expansion=true)?$");

	private ForumHtmlParser parser = new ForumHtmlParser();

	/**
	 * Our implementation to determine whether or not to visit the provided URL.
	 * 
	 * @param referringPage
	 *            is not used
	 * @param url
	 *            is the URL to visit
	 * @return {@code true} if the crawler should visit the url
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		return (
		// visit the authentication pages to get the required cookies
		href.contains("www.play.net/remote/validation.asp") || href.startsWith("http://forums.play.net/return_from_pdn")
		// the return from authentication to takes you to the main forums page
				|| href.equals("http://forums.play.net/forums"))
				// crawl all of the DragonRealms forums
				|| href.startsWith("http://forums.play.net/forums/dragonrealms/")
						// don't follow a single post. we want to only visit
						// "pages" of posts
						&& !FILTER.matcher(href).find()
						// don't visit thread pages. we want to crawl the legacy
						// views to minimize duplicates.
						&& !href.contains("/thread/")
						// don't visit reply links
						&& !href.contains("reply_to=");
	}

	/**
	 * This function is called for all visited pages.
	 * 
	 * @param page
	 *            the visited {@link Page}.
	 */
	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();
		logger.debug("visited: {}", url);

		// only attempt to parse forum pages
		if (url.toLowerCase().startsWith("http://forums.play.net/forums/dragonrealms/")) {
			if (page.getParseData() instanceof HtmlParseData) {
				HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
				// attempt to parse posts on the page
				parser.parsePost(htmlParseData.getHtml());
			}
		}
	}
}
