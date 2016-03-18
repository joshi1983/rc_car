<?php
require_once('model.php');

class User extends Model
{
	protected $username;
	protected $password_hash;
	
	private static function hashPassword($password) {
		return $password;
	}
	
	public static function authenticate($username, $password) {
		
		
	}
}