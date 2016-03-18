<?php

class Model
{
	protected $id;
	protected static $defaultConnection;
	
	public function __construct($data = array()) {
		$this->id = intval($data['id']);
	}
	
	public static function setDefaultConnection($defaultConnection) {
		Model::$defaultConnection = $defaultConnection;
	}
	
	public function load() {
		
	}
	
	public function getData() {
		$result = array();
		foreach ($this as $key => $value) {
			$result[$key] = $value;
		}
		return $result;
	}
	
	public function getId() {
		return $this->id;
	}
	
}