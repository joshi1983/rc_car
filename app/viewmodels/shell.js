define(['durandal/plugins/router'],
function (router) {
		
function Shell() {
	var self = this;

	router.map([
		{ route: '', title: 'Remote Control Car', moduleId: 'viewmodels/rc_car', nav: true},
        { route: 'login', title: 'Login', moduleId: 'viewmodels/login', nav: true }
	]).buildNavigationModel();
	
	router.activate();
}

return Shell;

});