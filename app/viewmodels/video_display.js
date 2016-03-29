define(['knockout'],
function (ko) {

function VideoDisplay() {
	var self = this;	
	var is_recording = ko.observable(false);
	var image_url = ko.observable();
	var refreshRate = 100;
	var refreshPreferencesRate = 3000;
	var refreshPreferencesTimerId = undefined;
	var timerId = undefined;

	self.getImageURL = function() {
		return image_url();
	};
	
	self.isRecording = function() {
		return is_recording();
	};
	
	self.startRecording = function() {
		is_recording(true);
		$.ajax({
			'url': 'api/startRecording',
			'method': 'POST',
			'error': function() {
				console.log('Problem in stopRecording ajax call');
			}
		});
	};
	
	self.stopRecording = function() {
		is_recording(false);
		$.ajax({
			'url': 'api/stopRecording',
			'method': 'POST',
			'error': function() {
				console.log('Problem in stopRecording ajax call');
			}
		});
	};
	
	function downloadPreferences() {
		$.ajax({
			'url': 'api/preferences',
			'method': 'GET',
			'dataType': 'json',
			'success': function(response) {
				console.log('got response for preferences: ' + response);
				if ( response.is_recording === true || response.is_recording === false )
					is_recording(!!response.is_recording);
			},
			'error': function() {
				console.log('error happened while getting preferences.');
			}
		});
	}
	
	function updateDisplay() {
		var newValue = (new Date()).getTime();
		var url = 'api/getCameraFrame?t=' + newValue;
		
		// use a trick to preload the image.
		// trick suggested at: http://stackoverflow.com/questions/8647305/preload-background-image
		var $img = $( '<img src="' + url + '">' );
		$img.bind( 'load', function(){
			image_url(url);
		} );
		if( $img[0].width ){ $img.trigger( 'load' ); }
	}
	
	function updateAutoRefresher() {
		if (is_recording() && !timerId) {
			timerId = window.setInterval(updateDisplay, refreshRate);
		}
		else if (!is_recording() && timerId) {
			window.clearInterval(timerId);
			timerId = undefined;
		}
	}
	
	is_recording.subscribe(updateAutoRefresher);
	
	self.activate = function() {
		updateAutoRefresher();
		downloadPreferences();
		refreshPreferencesTimerId = window.setInterval(downloadPreferences, refreshPreferencesRate);
	};
	
	self.detached = function() {
		window.clearInterval(refreshPreferencesTimerId);
		if (timerId)
			window.clearInterval(timerId);
	};
}

return VideoDisplay;

});