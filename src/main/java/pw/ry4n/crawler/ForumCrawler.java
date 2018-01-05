package pw.ry4n.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import pw.ry4n.parser.ForumHtmlParser;

public class ForumCrawler extends WebCrawler {
	private static final Logger logger = LoggerFactory.getLogger(ForumCrawler.class);

	ForumHtmlParser parser = new ForumHtmlParser();

	/**
	 * This method receives two parameters. The first parameter is the page in
	 * which we have discovered this new url and the second parameter is the new
	 * url. You should implement this function to specify whether the given url
	 * should be crawled or not (based on your crawling logic). In this example,
	 * we are instructing the crawler to ignore urls that have css, js, git, ...
	 * extensions and to only accept urls that start with
	 * "http://www.ics.uci.edu/". In this case, we didn't need the referringPage
	 * parameter to make the decision.
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		return (href.startsWith("http://forums.play.net/forums/dragonrealms/")
				|| href.startsWith("http://www.play.net/remote/validation.asp")
				|| href.startsWith("https://www.play.net/remote/validation.asp")
				|| href.startsWith("http://forums.play.net/return_from_pdn")
				|| href.equals("http://forums.play.net/forums"))
				&& !href.contains("/thread/")
				&& !href.contains("reply_to=");
	}

	/**
	 * This function is called when a page is fetched and ready to be processed
	 * by your program.
	 */
	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();
 		logger.debug("URL: " + url);
 
 		if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            parser.parsePost(htmlParseData.getHtml());
 		}
	}
}
