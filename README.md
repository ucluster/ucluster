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

Effect at delivery property

Trailing example:

    mask: {
        trailing: 8
    }
    
if the original value is `510108198801011212` and `trailing` is `8`, the output is `5101081988********`. If the original value length is less than `trailing` indicates, then it will return value with original length and are content are masked     

Leading example:

    mask: {
        leading: 8
    }
    
if the original value is `510108198801011212` and `leading` is `8`, the output is `********8801011212`. If the original value length is less than `leading` indicates, then it will return value with original length and are content are masked    

Range example:

    mask: {
        from: 6
        to: 14
    }
    
if the original value is `510108198801011212` and `from` is `6` and `to` is `14`, the output is `510108********1212`. If the original value length is less than required, then it will return value with original length and are content are masked    


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

            void effect(Record record, String path);

            Object configuration();
        }

Step 2: `String record()`: provide unique identifier of the validator

Step 3: `boolean isAbout(Point point)`: checks which lifecycle step this concerns are cares about

Step 4: `void effect(Record record, String path)`: handle the logic when concern effects

Step 5: `Object configuration();`: provide configuration of this validator

Example: 

    format: {
        pattern: "\\w{6,16}"
    }

    a) method `record` is fixed to return string `format`
    b) method `validate` is using java regex pattern to verify does property in path of request has satisfied
    c) method `configuration` return map representation of 

    format: {
        pattern: "\\w{6,16}"
    }
            
convention is JSON being used   
   
## Verification(Doing)
   
verify user actually holds the confirmation way.
   
Example:
   
       user({
           ... ...
           email: {
               email: true,
               identity: true
           }
           ... ...
       })
       .verify('email').using('email');
   
In the example above, this means when user want to register, it must be confirmed by email. `verify(<property>)` indicates how the user should be verified/confirmed, `method` can be email, SMS, phone call, etc. `using(<method>)` means validate against which property in case that user have multi property can be confirmed the same way.   

## Dev-Env

    cd dev;
    vagrant up --provision

## TODO-LIST

###Role Based Access Control(High)

https://github.com/mdarby/restful_acl
http://stackoverflow.com/questions/190257/best-role-based-access-control-rbac-database-model

###Jackson Polymorphic Support(High)

easier plugin implementation with less code

references:

https://www.dilipkumarg.com/dynamic-polymorphic-type-handling-jackson/
http://www.cowtowncoder.com/blog/archives/2010/03/entry_372.html

###Nested Property Support(Medium)

easier to understand from domain point of view

###User Definition Migration Support(Low) 

###Split Json Representation(Undefined)

###Pretty Dirty Tracker

https://vladmihalcea.com/2014/08/21/the-anatomy-of-hibernate-dirty-checking/
https://vladmihalcea.com/2016/02/11/how-to-enable-bytecode-enhancement-dirty-checking-in-hibernate/
    
###List Property Support
    
for example, user can provide multi mobile phone or email address for contact    

###Verification Frequency Control

captcha? method: always, by_frequency, etc. may refer topbeat

###Session Rename(Medium)

currently session just an interface for Redis, find a better name to represent what actually doing. Currently session is also used for store things like auth code.

###Guice Registration Refactor(Medium)

Using patterns like 'registry'

###Refactor modules to make it clear for implementation(Medium)

###Query Object(Low)