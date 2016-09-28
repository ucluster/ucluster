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
        format: {
            pattern: "[a-z0-9!#$%&'*+\/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+\/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"
        }
    },
    nickname: {
        format: {
            pattern: "\\w{6,16}"
        }
    }
});