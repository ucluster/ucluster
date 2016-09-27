user({
    username: {
        required: true,
        uniqueness: true,
        format: {
            pattern: "\\w{6,16}"
        }
    },
    password: {
        password: true,
        required: true,
        format: {
            pattern: "\\w{6,16}"
        }
    }
});