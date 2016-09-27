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

#### required

Example:
    
    required: true
    
NOTE*: by convention, default `required` is `false`    

#### format

Example:

    format: {
        pattern: "\\w{6,16}"
    }
    
#### customize validator    

Step 1: Inherit interface `PropertyValidator`

        public interface PropertyValidator {
        
            String type();
            
            ValidationResult validate(Map<String, Object> request, String propertyPath);
            
            Object configuration();
        }

Step 2: `String type()`: provide unique identifier of the validator

Step 3: `ValidationResult validate(Map<String, Object> request, String propertyPath)`: validate against the request

Step 4: `Object configuration();`: provide configuration of this validator

Example: 

    format: {
        pattern: "\\w{6,16}"
    }

a) method `type` is fixed to return string `format`
b) method `validate` is using java regex pattern to verify does property in propertyPath of request has satisfied
c) method `configuration` return map representation of 

    format: {
        pattern: "\\w{6,16}"
    }
            
convention is JSON being used      

## Dev-Env

    cd dev;
    vagrant up --provision

## TODO-LIST

    1. plugin-in supported
    2. provide common plugin