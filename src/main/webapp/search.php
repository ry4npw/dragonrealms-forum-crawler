<?php
$dir = 'sqlite:forum.db';
 
$dbh = new PDO($dir) or die("cannot open database");

$stmt = $dbh->prepare('SELECT folder, post_number as post_id, time as date, subject, snippet(forumdata) as snippet FROM forumdata WHERE forumdata match ?');
$stmt->execute(array($_GET['q']));
echo json_encode($stmt->fetchAll(PDO::FETCH_ASSOC), JSON_UNESCAPED_SLASHES);
?>
