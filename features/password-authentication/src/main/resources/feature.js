feature('password-authentication')
    .configuration({
        identities: ['username'],
        password: ['password']
    })
    .user({
        password: {
            required: true,
            credential: true
        }
    })
    .auth_method('password');