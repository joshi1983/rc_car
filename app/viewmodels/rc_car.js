define(['durandal/plugins/router', 'knockout', 'jquery'],
function (router, ko, $) {

// a viewmodel for the remote control page application
function RcCar() {
	var self = this;

	self.steering_value = ko.observable(0);
	self.drive_value = ko.observable(0);
	self.latest_steering_value = ko.observable(5);
	self.latest_drive_value = ko.observable(5);
	
	function sendStateToServer() {
		var carState = {
			'steering_value': self.steering_value(),
			'speed_value': self.drive_value()
		};
		
		// send message to server.
		$.ajax({
			'url': '/rc_car/api/saveDesiredState',
			'method': 'post',
			'data': carState
		});
	}

	function retrieveState() {
		// send message to server.
		$.ajax({
			'url': '/rc_car/api/carState',
			'type': 'json',
			'success': function(response) {
				self.steering_value(response.desired.steering_value);
				self.drive_value(response.desired.speed_value);
				self.latest_steering_value(response.latest.steering_value);
				self.latest_drive_value(response.latest.speed_value);
				console.log(JSON.stringify(response));
			}
		});
	}
	retrieveState();
	
	self.steering_value.subscribe(sendStateToServer);
	self.drive_value.subscribe(sendStateToServer);
}

return RcCar;

});