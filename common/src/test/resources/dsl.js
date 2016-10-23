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
    },
    email: {
        email: true,
        identity: true
    }
}).verify("email").using("email");