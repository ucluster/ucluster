var definition = {};
var action_definition = {};
var verification_definition = {}

function Action() {
    var target = this;
    this.on = function (type, action) {
        action_definition[type] = action;
        return target;
    }
    this.verify = function(property) {
        verification_definition[property] = property;
        that = this;
        return {
           target: property,
           using: function(method) {
             verification_definition[this.target] = method;
             return that;
           }
        }
    }
}


var user = function (user) {
    definition = user;
    return new Action();
};

var request = function (request) {
    definition = request;
    return new Action();
};