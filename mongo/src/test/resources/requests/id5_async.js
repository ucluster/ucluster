request({
    id_number: {
        required: true,
        format: {
            pattern: "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$"
        }
    },
    id_name: {
        required: true
    }
}).on("approve", {})
    .on("reject", {
        reason: {
            required: true
        }
    });