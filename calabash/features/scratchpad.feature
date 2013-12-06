Feature: Scratches
  Scenario: As unauthenticated user I should start with no items in my scratchpad
    Given I see the text "Login"
    And I click the "Scratchpad" menu item
    Then I should see text containing "no scratches yet"

  Scenario: As unauthenticated user I can add a file to the scratchpad
    Given I see the text "Login"
    And I click the "Scratchpad" menu item
    Then I should see text containing "no scratches yet"
    And I press view with id "action_scratchpad_new_markdown"
    Then I enter text "Hi there" into field with id "markdownEditor"
    Then I press view with id "action_save_as_scratch"
    Then I should see "Hi there"
    Then I should see text containing "Updated:"

  Scenario: I can delete an item from the scratchpad
    Given I see the text "Login"
    And I click the "Scratchpad" menu item
    Then I long press "Hi there"
    Then I press "Yes"
    Then I should see text containing "no scratches yet"


