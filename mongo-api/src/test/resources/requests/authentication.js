request({
    identity_property: {
        required: true
    },
    identity_value: {
        required: true
    },
    credential_property: {
        required: true
    },
    credential_value: {
        required: true
    }
}).on('approve', {})
    .on('reject', {});