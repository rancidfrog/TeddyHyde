Feature: Contextual Transforms

  Scenario: I get contextual transforms
    Given I have logged in
    And I access the "mynewblog" repository
    Then I wait for 10 seconds
    And I select "_posts"
    And I wait for 3 seconds
    And I select "2014-04-22-testing-impress.md"
    And I wait for the editor
    Then I should see "90degrees" in the hyde menu
    And I wait for 30 seconds

  Scenario: If not the correct template, then I ignore contextual transforms
    Given I have logged in
    And I access the "mynewblog" repository
    Then I wait for 10 seconds
    And I select "_posts"
    And I wait for 3 seconds
    And I select "2013-11-11-my-new-file.md"
    And I wait for the editor
    Then I should not see "90degrees" in the hyde menu
    And I wait for 30 seconds