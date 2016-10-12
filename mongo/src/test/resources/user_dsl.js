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
    id_number: {
        format: {
            pattern: "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$"
        }
    },
    id_name: {},
    nickname: {
        format: {
            pattern: "\\w{6,16}"
        }
    }
});