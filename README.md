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
            credential: true,
            required: true,
            format: {
                pattern: "\\w{6,16}"
            }
        }
    });     
    
User can be updated by request, requests DSL example:

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
    })
    .on("approve", {})
    .on("reject", {
        reason: {
            required: true
        }
    });
    
    
### Property Definition
    
In above example, `username` and `password` are two user properties definition, currently property definition are composed by property concerns

*NOTE: currently nested property are not supported yet.

### Property Lifecycle

validation -> before_create -> create

validation -> before_update -> update

see http://guides.rubyonrails.org/active_record_basics.html#callbacks as a reference

### Property Concern

Property concerns take care of part of property logic, such as validation, encryption, etc. 

#### identity 

*(required by authentication: authentication need one property as identity and one property as password)*

Example:

    identity: true

default `identity` is `false`.

#### credential

*(required by authentication: authentication need one property as identity and one property as password)*

if property is declared as `credential`, then it will encrypted(BCrypt) before save to database, and this property cannot be searched

default value is `false`

#### immutable

if property is declared as `immutable`, then it will cannot be updated, and exception will be thrown

default value is `false`

#### register validator

    registerConcern("format", FormatValidator.class);
    
NOTE: in the example above `format` is internally used as `@Named("property.format.concern")`, the convention is `property.<validator_type>.concern`.
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
    
#### transient
    
Example:
    
    transient: true

default `transient` is `false`
    
for request like authentication, you may not want to store the request property, so you can declare it as transient, transient is effected for BEFORE_UPDATE and BEFORE_CREATE, so validation of the property is still working     

#### mask

Example:

    mask: {
        trailing: 8
    }
    
if property configured as mask, it will automatically mask the original property value, for example: if the original value is `510108198801011212` and `trailing` is `8`, the output is `5101081988********`     

#### hidden

Example:

    hidden: true
    
default `transient` is `false`

if value is `true`, then will not returned to api
    
#### customize concern    

Step 1: Inherit interface `Record.Property.Concern`

        interface Concern {

            String record();

            boolean isAbout(Point point);

            void effect(Record record, String propertyPath);

            Object configuration();
        }

Step 2: `String record()`: provide unique identifier of the validator

Step 3: `boolean isAbout(Point point)`: checks which lifecycle step this concerns are cares about

Step 4: `void effect(Record record, String propertyPath)`: handle the logic when concern effects

Step 5: `Object configuration();`: provide configuration of this validator

Example: 

    format: {
        pattern: "\\w{6,16}"
    }

    a) method `record` is fixed to return string `format`
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