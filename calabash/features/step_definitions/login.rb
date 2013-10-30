
Given /I start the login process/ do
      touch "button"
end   

And /I acknowledge the 2 factor bug/ do
      tv = query( "textview", "text" )
      if tv[0] =~ /2\-factor authentication/ 
      touch "button text:'Ok'"
      end   
end   


And /I select "New Post"/ do
    touch "ActionMenuItemView id:'action_add_new_post'"
end

Then(/^I should see the text containing "(.*?)"$/) do |arg1|
   if arg1 =~ /markdown/i
   puts "Got text!"
   end
end

And /I choose a filename/ do
    query "edittext", :setText => "My first file"
end

And /I login using GitHub oAuth login/ do
      set_text "webView css:input[name=login]", "burning@burningon.com"
      set_text "webView css:input[name=password]", "L0udCaf3"
      touch "webView input:'button[type=submit]'"
      sleep 10
      text = query( "webView css:'.bubble-content'", "textContent" )[0]
      if text =~ /Teddy Hyde Android wants/
            performAction "scroll_to", "css", "a.oauth-deny"
      	    touch "webView css:'button[name=authorize]'"
      end	    
      sleep 10
end    

And /I access the first repository/ do
touch( "listview textview" )
end 

