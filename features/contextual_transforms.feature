Feature: Contextual Transforms

  Scenario: As a user I get contextual transforms
    Given I have logged in
    And I access the "mynewblog" repository
    Then I wait for 10 seconds
    And I select "_posts"
    And I wait for 3 seconds
    And I select "2014-04-22-testing-impress.md"
    And I wait for the editor
    Then I should see "90degrees" in the menu