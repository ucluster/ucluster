var definition = {};
var action_definition = {};
var confirmation_definition = {};

function Record() {
    var target = this;
    this.on = function (type, action) {
        action_definition[type] = action;
        return target;
    };
    this.confirm = function (property) {
        definition[property] << {"confirm": property}
        return {
            confirming: property,
            using: function (method) {
                definition[this.confirming]["confirm"] = method;
                return target;
            }
        }
    };
}

var user = function (user) {
    definition = user;
    return new Record();
};

var request = function (request) {
    definition = request;
    return new Record();
};