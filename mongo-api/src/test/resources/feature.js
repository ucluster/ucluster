feature('nickname')
    .user({
        nickname: {
            format: {
                pattern: "\\w{6,16}"
            }
        }
    })
    .request('update_nickname', {
        nickname: {
            required: true,
            format: {
                pattern: "\\w{6,16}"
            }
        }
    });
