# UCluster (IN-DEV)

user authentication framework

## User Definition DSL
    
Example:
     
    user({
        username: {
            identity: true,
            immutable: true,
            format: {
                pattern: "\\w{6,16}"
            }
        },
        password: {
            password: true,
            required: true,
            format: {
                pattern: "\\w{6,16}"
            }
        }
    });     
    
### Property Definition
    
In above example, `username` and `password` are two user properties definition, currently property definition are composed by property validators, more metadata related definition will come later

*NOTE: currently nested property are not supported yet.

### Property Lifecycle

validation -> before_create

validation -> before_update

see http://guides.rubyonrails.org/active_record_basics.html#callbacks as a reference

(TBD)

### Property Processor


#### identity 

*(required by authentication: authentication need one property as identity and one property as password)*

Example:

    identity: true

default `identity` is `false`. `identity` is a combination of `required` and `uniqueness`

#### password

*(required by authentication: authentication need one property as identity and one property as password)*

if property is declared as `password`, then it will encrypted(BCrypt) before save to database, and this property cannot be searched

default value is `false`

#### immutable

if property is declared as `immutable`, then it will cannot be updated, and exception will be thrown

default value is `false`

### Property Validator

In above example, `required`, `uniqueness`, `format` are property validators, which are used when you create new user or update user properties

#### register validator

    registerConcern("property.format.validator", FormatValidator.class);
    
NOTE: in the example above `property.format.validator` is internally used as `@Named("property.format.validator")`, the convention is `property.<validator_type>.validator`.
After registration, you can use this validator as `<validator_type>: { ...<configuration_json>...}`

#### required

Example:
    
    required: true
    
default `required` is `false`    

#### format

Example:

    format: {
        pattern: "\\w{6,16}"
    }
    
#### uniqueness
    
Example:    

    uniqueness: true
    
default `uniqueness` is `false`    

#### email

Example:
    
    email: true
    
default `email` is `false`

email validator regex patterns: 
positive pattern: 
    
    [a-z0-9!#$%&'*+"=?^_`{|}~-]+(?:.[a-z0-9!#$%&'*+"=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?

negative pattern:

    .*@10minutemail\.com|.*@dreggn\.com
    
#### customize validator    

Step 1: Inherit interface `PropertyValidator`

        public interface PropertyValidator {
        
            String type();
            
            ValidationResult validate(Map<String, Object> request, String propertyPath);
            
            Object configuration();
        }

Step 2: `String type()`: provide unique identifier of the validator

Step 3: `ValidationResult validate(Map<String, Object> request, String propertyPath)`: validate against the request. You can use @Inject here to access UserRepository to validate against with existing user data

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

    1. support multi user definition at the same time(how to distinguish group?)
    2. user definition migration support 
    3. plugin-in supported
    4. provide common plugin
    5. consider using compile-time instrumentation to encapsulate model, as well as generate models      