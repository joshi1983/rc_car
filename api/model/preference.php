<?php

require_once('model.php');

class Preference extends Model
{
	protected $is_recording;
		
	public function __construct($data = array()) {
		parent::__construct($data);
		
		if (isset($data['is_recording']))
			$this->is_recording = intval($data['is_recording']);
	}
	
	public function getData() {
		$result = parent::getData();
		$result['is_recording'] = boolval($result['is_recording']);
		
		return $result;
	}
	
}
