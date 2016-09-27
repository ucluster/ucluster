user({
    username: {
        required: true,
        uniqueness: true,
        format: {
            pattern: "\\w{6,16}"
        }
    },
    password: {
        required: true,
        encrypted: true,
        format: {
            pattern: "\\w{6,16}"
        }
    }
});