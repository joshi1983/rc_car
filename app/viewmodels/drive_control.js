define(['durandal/plugins/router', 'knockout'],
function (router, ko) {

function DriveControl() {
	var self = this;

	self.activate = function(activationData) {
		if( !ko.isObservable(activationData.drive_value) )
			throw new Error("activationData.drive_value is required to be observable");
		if( !ko.isObservable(activationData.latest_echoed_drive_value) )
			throw new Error("activationData.latest_echoed_drive_value is required to be observable");
		
		self.drive_value = activationData.drive_value;
		self.latest_echoed_drive_value = activationData.latest_echoed_drive_value;
	};
}

return DriveControl;

});