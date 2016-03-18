define([],
function () {

function UniqueGenerator() {
	
}

var id = 0;

UniqueGenerator.generateUniqueId = function() {
	return 'unique-id-' + (++id);
};

return UniqueGenerator;

});