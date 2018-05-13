package pw.ry4n.crawler;

import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import pw.ry4n.db.SQLiteDbService;
import pw.ry4n.db.SQLiteDbServiceImpl;
import pw.ry4n.parser.ForumHtmlParser;

public class ForumCrawler extends WebCrawler {
	private static final Logger logger = LoggerFactory.getLogger(ForumCrawler.class);

	private static final Pattern FILTER = Pattern.compile("\\/view\\/\\d+(\\?force_expansion=true)?$");

	private ForumHtmlParser parser;

	private static final SQLiteDbService service = new SQLiteDbServiceImpl("forum.db");

	private Map<String, Long> lastScanFolderSize;

//	private static final Set<String> pagesToNotParse = new HashSet<>();

	public ForumCrawler() {
		// populate the list
		lastScanFolderSize = service.getUniqueFolderNamesAndMaxPostNumber();
		parser = new ForumHtmlParser(lastScanFolderSize);
	}

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
		String href = url.getURL().toLowerCase().replace("%20%20", "%20");
		return (
		// visit the authentication pages to get the required cookies
		(href.contains("www.play.net/remote/validation.asp")
				|| href.startsWith("http://forums.play.net/return_from_pdn")
				// the return from authentication to takes you to the main
				// forums page
				|| href.equals("http://forums.play.net/forums"))
				// crawl all of the DragonRealms forums
				|| href.startsWith("http://forums.play.net/forums/dragonrealms/"))
				// don't follow a single post. we want to only visit "pages" of
				// posts
				&& !FILTER.matcher(href).find()
				// don't visit thread pages. we want to crawl the legacy views
				// to minimize duplicates.
				&& !href.contains("/thread/")
				// don't visit reply links
				&& !href.contains("reply_to=")
				// page 1 is the same result as /view so don't re-visit it
				&& !href.endsWith("&page=1");
	}

//	private boolean shouldVisitPageNumber(String href) {
//		for (String key : lastScanFolderSize.keySet()) {
//			if (href.contains(key)) {
//				if (href.endsWith("/view") && lastScanFolderSize.get(key).intValue() > 0) {
//					// need to visit at least the first page
//					pagesToNotParse.add(href);
//					return true;
//				}
//
//				// check for page > maxPostNumber / 40
//				int pagePosition = href.lastIndexOf("page=");
//				if (pagePosition > -1) {
//					int pageNumber = Integer.parseInt(href.substring(pagePosition + 5));
//					int processedPages = lastScanFolderSize.get(key).intValue() / 40;
//					if (pageNumber < processedPages) {
//						return false;
//					}
//				}
//				break;
//			}
//		}
//		return true;
//	}

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

		String lowercaseUrl = url.toLowerCase();

		// only attempt to parse forum pages
		if (lowercaseUrl.startsWith("http://forums.play.net/forums/dragonrealms/")) {
			if (page.getParseData() instanceof HtmlParseData) {
				HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
				// attempt to parse posts on the page
				parser.parsePost(htmlParseData.getHtml());
			}
		}
	}
}
