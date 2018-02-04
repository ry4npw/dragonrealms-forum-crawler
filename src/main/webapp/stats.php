<?php
$dir = 'sqlite:forum.db';
$pdo = new PDO($dir) or die("cannot open database");
$count = $pdo->query('select count(*) from forumdata')->fetchColumn();
echo "{\"indexed\":",filemtime('forum.db'),",\"rows\":",$count,"}"
?>
