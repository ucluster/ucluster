user({
    username: {
        required: true,
        uniqueness: true,
        immutable: true,
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
    },
    email: {
        email: true
    },
    nickname: {
        format: {
            pattern: "\\w{6,16}"
        }
    }
});