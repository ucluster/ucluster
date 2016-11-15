user({
    username: {
        required: true,
        immutable: true,
        format: {
            pattern: "\\w{6,16}"
        }
    }
});