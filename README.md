# UCluster (IN-DEV)

user authentication framework

## User Definition DSL
    
Example:
     
    user({
        username: {
            required: true,
            uniqueness: true,
            format: {
                pattern: "\\w{6,16}"
            }
        },
        password: {
            required: true,
            encrypted: true,
            format: {
                pattern: "\\w{6,16}"
            }
        }
    });     
    
### Property Definition
    
In above example, `username` and `password` are two user properties definition, currently property definition are composed by property validators, more metadata related definition will come later

*NOTE: currently nested property are not supported yet.

### Property Validator

In above example, `required`, `uniqueness`, `format` are property validators, which are used when you create new user or update user properties

## Dev-Env

    cd dev;
    vagrant up --provision

## TODO-LIST

    1. plugin-in supported
    2. provide common plugin