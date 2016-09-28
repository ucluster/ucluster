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
    },
    email: {
        format: {
            pattern: "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}"
        }
    },
    nickname: {
        format: {
            pattern: "\\w{6,16}"
        }
    }
});