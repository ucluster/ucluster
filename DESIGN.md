# Thinking in Rails ActiveRecord

##Rails Concern

###Traditional Way to Define a User

    class User < ActiveRecord::Base
      has_secure_password
    
      def self.authenticate(email, password)
        user = find_by_email(email)
        user if !user.nil? && user.authenticate(password)
      end
    
      def create_password_reset_token
        logger.warn "Create password reset token code goes here."
        false
      end
    end

###Concern Way to Define a User:

Create a concern

    module Authentication
      extend ActiveSupport::Concern
    
      included do
        has_secure_password
      end
    
      module ClassMethods
        def authenticate(email, password)
          user = find_by_email(email)
          user if user && user.authenticate(password)
        end
      end
    
      def create_password_reset_token
        logger.warn "Create password reset token code goes here."
        false
      end
    end
    
then define User    

    class User < ActiveRecord::Base
      include Authentication
    end

*references*:
https://richonrails.com/articles/rails-4-code-concerns-in-active-record-models

*source code*:
https://github.com/rails/rails/blob/master/activesupport/lib/active_support/concern.rb

##Rails Callbacks

### Callback extends Concern

    module Callbacks
      extend ActiveSupport::Concern

    def define_model_callbacks(*callbacks)
      options = callbacks.extract_options!
      options = {
        terminator: deprecated_false_terminator,
        skip_after_callbacks_if_terminated: true,
        scope: [:kind, :name],
        only: [:before, :around, :after]
      }.merge!(options)

      types = Array(options.delete(:only))

      callbacks.each do |callback|
        define_callbacks(callback, options)

        types.each do |recordGroup|
          send("_define_#{recordGroup}_model_callback", self, callback)
        end
      end
    end  

autosave
      
    def add_autosave_association_callbacks(reflection)

  
        
Q: How to extend rails lifecycle        
            
*source code*        
https://github.com/rails/rails/blob/master/activerecord/lib/active_record/autosave_association.rb    



*source code*:
https://github.com/rails/rails/blob/master/activerecord/lib/active_record/callbacks.rb

see http://guides.rubyonrails.org/active_record_basics.html#callbacks as a reference