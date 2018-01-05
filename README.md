# dragonrealms-forum-crawler

This is a Java-based web crawler that will crawl [forums.play.net](https://forums.play.net/forums/DragonRealms/view) for posts and save them to a SQLite database. Posts are added to a Full Text Search (FTS) virtual table which can then be accessed using FTS match queries.

For a front-end, a single-page HTML queries the SQLite database using a RESTful PHP search endpoint.

## 1. Build

### Prerequisites

1. Install the [Java SE JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
2. Install [maven](https://maven.apache.org/install.html)
3. Run the maven install

```
$ mvn install
```

## 2. Create the forum.db

From the commandline run:

```
$ java -jar target/dragonrealms-forum-crawler.jar
```

## 3. Running locally

If you have PHP installed (PHP is installed by default on Mac OS X), you can run the front end using the following commands:

```
$ mv forum.db src/main/webapp
$ cd src/main/webapp
$ php -S localhost:8000
```

Navigate to http://localhost:8000 in your browser.

## 4. Deployment

Copy the forum.db and all files in src/main/webapp to your PHP server.

