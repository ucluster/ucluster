user({
    username: {
        identity: true,
        immutable: true,
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
    },
    email: {
        email: true,
        identity: true
    },
    nickname: {
        format: {
            pattern: "\\w{6,16}"
        }
    }
});