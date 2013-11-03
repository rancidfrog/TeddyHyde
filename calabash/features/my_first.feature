Feature: Add file to repository
  Scenario: As a logged in user I can add a file
    Given I start the login process
    And I acknowledge the 2 factor bug
    Then I wait for 10 seconds
    And I login using GitHub oAuth login
    And I access the first repository
    Then I wait for 10 seconds
    And I select "New Post" 
    And I choose a filename
    And I press "Ok"
    Then I should see the text containing "markdown"
