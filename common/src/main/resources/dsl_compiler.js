var definition = {};
var action_definition = {};

function Action() {
    var target = this;
    this.on = function (type, action) {
        action_definition[type] = action;
        return target;
    }
}

var user = function (user) {
    definition = user;
};

var request = function (request) {
    definition = request;
    return new Action();
};