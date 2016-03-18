define(['knockout'],
function (ko) {

function SteeringControl() {
	var self = this;

	self.activate = function(activationData) {
		if( !ko.isObservable(activationData.steering_value) )
			throw new Error('activationData.steering_value required to be observable.');
		if( !ko.isObservable(activationData.latest_echoed_steering_value) )
			throw new Error("activationData.latest_echoed_steering_value is required to be observable");
		
		self.steering_value = activationData.steering_value;
		self.latest_echoed_steering_value = activationData.latest_echoed_steering_value;		
	};
}

return SteeringControl;

});