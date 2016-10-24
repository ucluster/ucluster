request({
    nickname: {
        format: {
            pattern: "\\w{6,16}"
        }
    }
}).on('approve', {}).on('reject', {});
