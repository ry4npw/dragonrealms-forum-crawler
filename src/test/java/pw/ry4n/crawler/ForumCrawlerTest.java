package pw.ry4n.crawler;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.uci.ics.crawler4j.url.WebURL;

public class ForumCrawlerTest {
	ForumCrawler crawler = new ForumCrawler();

	@Test
	public void testShouldVisit() {
		// positive tests
		assertTrue(crawler.shouldVisit(null, url("http://forums.play.net/forums/DragonRealms/view")));
		assertTrue(crawler.shouldVisit(null, url("http://forums.play.net/forums/DragonRealms/Discussions%20with%20DragonRealms%20Staff%20and%20Players/view")));
		assertTrue(crawler.shouldVisit(null, url("http://forums.play.net/forums/DragonRealms/Discussions%20with%20DragonRealms%20Staff%20and%20Players/Game%20Master%20and%20Official%20Announcements/view")));

		// negative tests
		assertFalse(crawler.shouldVisit(null, url("http://forums.play.net/forums/DragonRealms/Discussions%20with%20DragonRealms%20Staff%20and%20Players/Game%20Master%20and%20Official%20Announcements/view/1539")));
		assertFalse(crawler.shouldVisit(null, url("http://forums.play.net/forums/13/126/1025/post?reply_to=1869407")));
		assertFalse(crawler.shouldVisit(null, url("http://forums.play.net/forums/DragonRealms/Discussions%20with%20DragonRealms%20Staff%20and%20Players/Game%20Master%20and%20Official%20Announcements/thread/1869408?get_newest=true")));
		assertFalse(crawler.shouldVisit(null, url("http://forums.play.net/forums/DragonRealms/The%20Rangers/Meeting%20Discussions/view/224?force_expansion=true")));
	}

	private WebURL url(String url) {
		WebURL webURL = new WebURL();
		webURL.setURL(url);
		return webURL;
	}
}
