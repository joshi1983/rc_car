<?php

function getMySQLConnection() {
	// load config with host name.
	$filename = '../db/config.json';
	// This will become a place for picking other filenames.
	
	$content = file_get_contents($filename);
	$config = json_decode($content);

	return $config;
}