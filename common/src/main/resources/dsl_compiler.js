var definition = {};
var feature_name = '';
var request_definitions = {};

function Record() {
    var target = this;
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

var user = function (u) {
    definition = u;
    return new Record();
};

var feature = function (name) {
    var target = this;
    feature_name = name;

    this.user = function (u) {
        definition = u;
        return target;
    };

    this.request = function (type, definition) {
        request_definitions[type] = definition;
        return target;
    };

    return this;
};