package pw.ry4n;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import pw.ry4n.crawler.ForumCrawler;
import pw.ry4n.db.SQLiteDbService;
import pw.ry4n.db.SQLiteDbServiceImpl;

/**
 * The main class that configures and launches our crawler.
 */
public class Launcher {
	public static void main(String[] args) throws Exception {
		// crawl storage folder for resumable crawling
		String crawlStorageFolder = "target/data/crawl/root";
		int numberOfCrawlers = 7;

		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(crawlStorageFolder);

		// Instantiate the controller for this crawl.
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

		/*
		 * Seed URLs that are the first pages fetched and then the crawler
		 * starts following links found in these pages
		 *
		 * One enters the DragonRealms forums through the /return_from_pdn URL.
		 * This URL takes the following parameters:
		 *
		 * "tcode" is the current time formatted as "yyyyMMddHHmmss"
		 *
		 * "num" appears to be a number between 10000 and 99999?
		 *
		 * "username" and "authentication" are not required fields
		 */
		controller.addSeed("http://forums.play.net/return_from_pdn?username=&tcode=" + sdf.format(new Date())
				+ "&authentication=&num=" + 10000 + (new Random()).nextInt(89999));
		controller.addSeed("http://forums.play.net/forums/view_type?type=legacy");

		/*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */
		controller.start(ForumCrawler.class, numberOfCrawlers);
		controller.waitUntilFinish();

		// create the FTS table
		SQLiteDbService service = new SQLiteDbServiceImpl("forum.db");
		service.createFTS();
		service.close();
	}
}
