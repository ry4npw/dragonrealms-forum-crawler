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
	private static final Pattern PAGE_FILTER = Pattern.compile("\\/view\\/\\d+(\\?force_expansion=true)?$");
	private static final SQLiteDbService service = new SQLiteDbServiceImpl("forum.db");

	private ForumHtmlParser parser;
	private Map<String, Long> lastScanFolderSize;

	public ForumCrawler() {
		// populate the list
		lastScanFolderSize = service.getUniqueFolderNamesAndMaxPostNumber();
		parser = new ForumHtmlParser(lastScanFolderSize);
	}

	/**
	 * Our implementation to determine whether or not to visit the provided URL.
	 * 
	 * @param referringPage is not used
	 * @param url           is the URL to visit
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
				// don't follow a single post. we want to only visit "pages" of posts
				&& !PAGE_FILTER.matcher(href).find()
				// don't visit threads. only crawl the legacy views to minimize duplicates.
				&& !href.contains("/thread/")
				// don't visit reply links
				&& !href.contains("reply_to=")
				// finally skip pages we already have data for
				&& shouldVisitPageNumber(href);
	}

	/**
	 * Determine whether we have been to the provided {@code href} before using the
	 * following logic:
	 * 
	 * <ol>
	 * <li>Check if the URL contains a page number. If not, visit the page.</li>
	 * <li>Divide the highest post number by 40 to get the number of processed
	 * pages.</li>
	 * <li>If page in the URL < number of processed pages, do not visit the
	 * page</li>
	 * <li>Otherwise, visit the page</li>
	 * </ol>
	 * 
	 * @param href the URL
	 * @return {@code true} when we've not seen this page before, otherwise
	 *         {@code false}
	 */
	private boolean shouldVisitPageNumber(String href) {
		int pagePosition = href.lastIndexOf("page=");
		if (pagePosition < 0) {
			// URL does not contain a page number, visit the page.
			return true;
		}

		// get the pageNumber. assumes page= is the last element on the href.
		int pageNumber = Integer.parseInt(href.substring(pagePosition + 5));
		if (pageNumber == 1) {
			// page is 1. do not re-visit /view pages.
			return false;
		}

		for (String key : lastScanFolderSize.keySet()) {
			if (href.contains(key)) {
				// check for page > maxPostNumber / 40
				int processedPages = lastScanFolderSize.get(key).intValue() / 40;
				if (pageNumber < processedPages) {
					// we've been here before, do not proceed
					return false;
				}

				// don't continue through loop, we found it
				break;
			}
		}

		// visit the page
		return true;
	}

	/**
	 * This function is called for all visited pages.
	 * 
	 * @param page the visited {@link Page}.
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
