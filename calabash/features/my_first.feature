Feature: Add file to repository
  Scenario: As a logged in user I can add a file
    Given I start the login process
    And I acknowledge the 2 factor bug
    Then I wait 10 seconds
    And I login using GitHub oAuth login
    And I access the first repository
    And I press "New Post"
	    
