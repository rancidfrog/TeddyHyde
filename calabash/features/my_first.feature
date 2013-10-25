Feature: Login feature

  Scenario: As a valid user I can log into my app
    When I press "Login"
    Then I press "OK"
    Then I wait up to 10 seconds to see "GitHub"
    Then I enter "burningon" into input field number 1
    Then I wait 10 seconds
    Then I enter "L0udCaf3" into input field number 2
    Then I wait 10 seconds
    Then I press "Login"
