requirejs.config({
  paths: {
    'text': '../js/lib/require/text',	  
	'durandal': '../js/lib/durandal/js',
	'knockout': '../js/lib/knockout/knockout-3.4.0.debug',
	'jquery': '../js/lib/jquery/jquery-2.2.0',
	'viewmodels': 'viewmodels',
	'plugins': '../js/lib/durandal/js/plugins',
	'transitions': '../js/lib/durandal/js/transitions',
	'utils': '../js/lib/utils'
  },
  urlArgs: "bust=" + (new Date()).getTime()
});


define(['durandal/system', 'durandal/app', 'durandal/viewLocator', 'knockout'],
function(system, app, viewLocator, ko) {
	var system = require('durandal/system'),
	app = require('durandal/app');
 
	system.debug(true);
 
	// Replace 'viewmodels' in the moduleId with 'views' to locate the view.
	// Look for partial views in a 'views' folder in the root.
	viewLocator.useConvention();

	app.title = 'RC Car';
	app.throwOnErrors = true;
 
	app.configurePlugins({
		router: true,
		dialog: true,
		widget: true
	}, './plugins');
 
	app.start().then(function() {
		app.setRoot('viewmodels/shell');
   });

});

