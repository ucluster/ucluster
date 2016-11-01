feature('password-authentication')
    .user({
        password: {
            required: true,
            credential: true,
            format: {
                pattern: "\\w{6,16}"
            }
        }
    })
    .auth('password', {
        identities: ['username'],
        password: 'password'
    });