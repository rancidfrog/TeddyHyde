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

  Scenario:
    Given I see the text "Login"
    And I click the "Scratchpad" menu item
    And I press view with id "action_scratchpad_new_markdown"
    Then I enter text "Hi there" into field with id "markdownEditor"
    And I expose the menu items
    Then I should not see the gist menu item

  Scenario: Once logged in I can create gists
    Given I have logged in
    And I click the "Scratchpad" menu item
    And I press view with id "action_scratchpad_new_markdown"
    Then I enter the current date into field with id "markdownEditor"
    And I expose the menu items
    Then I should see the gist menu item
    And I touch the gist menu item
    And I paste in the URL
    Then I should have a gist with the current date


