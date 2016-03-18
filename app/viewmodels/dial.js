define(['knockout', 'utils/unique_generator'],
function (ko, UniqueGenerator) {

function Dial() {
	var self = this;

	self.unique_id = UniqueGenerator.generateUniqueId();
	var minVal = ko.observable(-10);
	var maxVal = ko.observable(10);
	var value; // replaced in activate method
	var latest_echoed_value; // replaced in activate method
	
	function valueToRadians(value_to_represent) {
		var valueRatio = 0;
		if( minVal() < maxVal() )
			valueRatio = (value_to_represent - minVal() + maxVal()) / (maxVal() - minVal());
		
		return 2 * Math.PI * valueRatio + Math.PI / 2;
	}
	
	var drawDial = function() {
		var root_element = $('#' + self.unique_id);
		var w = root_element.width();
		var h = root_element.height();
		var canvas = root_element.find('canvas');
		canvas.attr('width', w);
		canvas.attr('height', h);
		canvas.height(h);
		canvas.width(w);
		var diameter = Math.min(w, h);
		var g = canvas[0].getContext('2d');
		g.strokeStyle = '#000';
		g.fillStyle = '#cde';
		g.beginPath();		
		var cx = w / 2;
		var cy = h / 2;
		var radius = diameter / 2;
		
		g.arc(cx, cy, radius, 0, 2 * Math.PI);
		
		var latest_angle = valueToRadians(latest_echoed_value());
		var angle = valueToRadians(value());
		
		g.closePath();
		g.fill();
		g.stroke();
		var lines = [
			{'angle': angle, 'colour': 'rgba(0,0,0,0.7)'}, 
			{'angle': latest_angle, 'colour': 'rgba(0,0,0,0.4)'}
			];
		for( var i in lines ) {
			var line = lines[i];
			
			g.strokeStyle = line.colour;
			g.beginPath();
			g.moveTo(cx, cy);
			g.lineTo(
				cx + radius * Math.sin(line.angle), 
				cy - radius * Math.cos(line.angle)
				);
			g.stroke();
			g.closePath();
		}
	};
	
	self.activate = function(activationData) {
		if( !ko.isObservable(activationData.value) )
			throw new Error('activationData.value must be observable.');
		if( !ko.isObservable(activationData.latest_echoed_value) )
			throw new Error('activationData.latest_echoed_value must be observable.');
		
		if( typeof activationData.minVal === 'number' )
			minVal(activationData.minVal);
		if( typeof activationData.maxVal === 'number' )
			maxVal(activationData.maxVal);

		// keep the max on the larger side by swapping if necessary.
		if( maxVal() < minVal() ) {
			var temp = maxVal();
			maxVal(minVal());
			minVal(temp);
		}
		
		value = activationData.value;
		latest_echoed_value = activationData.latest_echoed_value;
		value.subscribe(drawDial);
		latest_echoed_value.subscribe(drawDial);
	};
	
	self.compositionComplete = function() {
		drawDial();
	};
	
	self.dialClicked = function(data, event) {
		var root_element = $('#' + self.unique_id);
		if( root_element.length > 0 ) {
			var offset = root_element.offset();
			var h = root_element.height();
			var w = root_element.width();
			var x = event.pageX - offset.left - (w / 2);
			var y = event.pageY - offset.top - (h / 2);
			var angle = Math.atan2(y, x);
			var valueRatio = angle / (Math.PI * 2) + 0.5;
			var newValue = minVal() + (maxVal() - minVal()) * valueRatio;
			self.setValue(newValue);
		}
	};
	
	self.setValue = function(newValue) {
		if( typeof newValue !== 'number' || isNaN(newValue) )
			throw new Error('dial setValue requires number parameter.  newValue: ' + newValue);
			
		if( newValue < minVal() )
			newValue = minVal();
		if( newValue > maxVal() )
			newValue = maxVal();
		
		value(newValue);
	};
	
	self.getValue = function() {
		return value();
	};
}

return Dial;

});