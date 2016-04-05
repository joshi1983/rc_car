define(['durandal/plugins/router', 'utils/browser_info'],
function (router, browserInfo) {
		
function Shell() {
	var self = this;

	router.map([
		{ route: '', title: 'Remote Control Car', moduleId: 'viewmodels/rc_car', nav: true},
        { route: 'login', title: 'Login', moduleId: 'viewmodels/login', nav: true }
	]).buildNavigationModel();
	
	router.activate();
	
	self.isBrowserMobile = function() {
		return browserInfo.isMobile();
	}
}

return Shell;

});