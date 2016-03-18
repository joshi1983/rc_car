<?php
require_once('model.php');

class CarControlState extends Model {
	protected $steering_value;
	protected $speed_value;
	protected $is_desired;
		
	public function __construct($data = array()) {
		parent::__construct($data);
		
		if (isset($data['steering_value']))
			$this->steering_value = floatval($data['steering_value']);
		
		if (isset($data['speed_value']))
			$this->speed_value = floatval($data['speed_value']);
		
		if (isset($data['is_desired']))
			$this->is_desired = intval($data['is_desired']) === 1;
	}
	
	public static function getDesiredState() {
		$sql = 'select * from car_control_state where is_desired=1';
		
		$res = mysqli_query(Model::$defaultConnection, $sql);
		if( !$res )
			throw new Exception("Problem with query: ".$sql);
		
		assert($res);
		$result = $res->fetch_assoc();
		assert($result);
		$res->free();
		
		return new CarControlState($result);
	}
	
	public static function getLatestState() {
		$sql = 'select * from car_control_state where is_desired=0';
		
		$res = mysqli_query(Model::$defaultConnection, $sql);
		assert($res);
		$result = $res->fetch_assoc();
		assert($result);
		$res->free();
		
		return new CarControlState($result);
	}
}

