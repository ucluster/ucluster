feature('password-authentication')
    .user({
        password: {
            credential: true,
            required: true,
            format: {
                pattern: "\\w{6,16}"
            }
        }
    })
    .auth('password', {
        identities: ['username'],
        password: 'password'
    });

