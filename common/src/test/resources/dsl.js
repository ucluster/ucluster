user({
    username: {
        required: true,
        uniqueness: true,
        format: {
            pattern: "\\w{6,16}"
        }
    },
    password: {
        credential: true,
        required: true,
        format: {
            pattern: "\\w{6,16}"
        }
    }
});