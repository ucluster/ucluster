feature('password-authentication')
    .user({
        phone: {
            required: true,
            format: {
                pattern: "\\d{6,16}"
            }
        }
    })
    .auth('phone', {
        identities: ['username'],
        verification_code: 'confirmation_code'
    });
