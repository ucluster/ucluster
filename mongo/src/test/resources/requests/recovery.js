request({
    credential_property: {
        required: true
    },
    credential_value: {
        required: true
    }
}).on('approve', {}).on('reject', {});