feature('refresh-token')
    .user({
        password: {
            required: true,
            credential: true,
            format: {
                pattern: "\\w{6,16}"
            }
        }
    })
    .auth('refresh', {
        access_token: 'access_token',
        refresh_token: 'refresh_token'
    });
