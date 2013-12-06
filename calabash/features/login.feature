Feature: Login

  Scenario: As a logged in user I can add a file
    Given I have logged in
    And I access the "mynewblog" repository
    Then I wait for the "Loading repository data.." dialog to close
    Then I wait for the "Loading hyde transformations.." dialog to close
    And I wait for the repository layout
    Then I wait for 10 seconds
    And I select "New Post" 
    And I enter "My new file" as the filename
    And I press "Ok"
    And I wait for the editor
    Then I should see "Markdown"
    Then I select "Save File"
    Then I wait for 3 seconds
    Then I go back
    Then I go back
    Then I go back
    Then I go back
    Then I wait for 20 seconds
    And I access the "mynewblog" repository
    Then I wait for the "Loading repository data.." dialog to close
    Then I wait for the "Loading hyde transformations.." dialog to close
    And I wait for the repository layout
    Then I wait for 10 seconds
    Then I press "_posts"
    And I scroll until I see the "My new file" as a jekyll post
    Then I click on the list item named "My new file"
    Then I should see "Markdown"

  # Scenario: As a logged in user I can rotate the device
  #   # Given I start the login process
  #   # And I acknowledge the 2 factor bug
  #   Then I rotate device left
    

