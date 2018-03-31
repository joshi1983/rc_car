define(['knockout', 'utils/unique_generator'],
function (ko, UniqueGenerator) {

function RectangularValueRangeSelector() {
	var self = this;

	self.unique_id = UniqueGenerator.generateUniqueId();
	var minVal = ko.observable(-90);
	var maxVal = ko.observable(90);
	var value; // replaced in activate method
	var latest_echoed_value; // replaced in activate method
	
	var drawDial = function() {
		var root_element = $('#' + self.unique_id);
		var w = root_element.width();
		var h = root_element.height();
		var canvas = root_element.find('canvas');
		canvas.attr('width', w);
		canvas.attr('height', h);
		canvas.height(h);
		canvas.width(w);
		var lineWidthRatio = 0.015;
		var diameter = Math.min(w, h) * (1 - (2 * lineWidthRatio));
		var g = canvas[0].getContext('2d');
		var lineWidth = Math.ceil(diameter * lineWidthRatio);
		g.strokeStyle = '#000';
		g.lineWidth = lineWidth;
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
		$(window).on('resize orientationChange', drawDial);
	};

	self.detached = function() {
		$(window).off('resize orientationChange', drawDial);
	};

	self.rangeClicked = function(data, event) {
		var root_element = $('#' + self.unique_id);
		if( root_element.length > 0 ) {
			var offset = root_element.offset();
			var h = root_element.height();
			var w = root_element.width();
			var x = event.pageX - offset.left - (w / 2);
			var valueRatio = x * 1.0 / w;
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
	
	self.getFormattedValue = function() {
		var result = "" + value();
		var decimalsPrecision = 2;
		var factor = Math.pow(10, decimalsPrecision);
		result = Math.round(result * factor) / (1.0 * factor);
		
		return result;
	};
}

return Dial;

});