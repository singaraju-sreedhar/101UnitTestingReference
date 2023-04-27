Feature: Reading Items

  Scenario: Reading all items
    Given items exists
    When items are read
    Then the items are returned
