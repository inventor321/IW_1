Feature: sample karate test script
  for help, see: https://github.com/intuit/karate/wiki/IDE-Support

  Background:
    * url baseUrl

  Scenario: get all events and then get the first event by id
    Given path '/events'
    When method get
    Then status 200

    * def first = response[0]

    Given path 'events', first.id
    When method get
    Then status 200

  Scenario: create an event and then get it by id

    Given path '/events/create'    
    And request { "name": "Test Event", "description": "bla bla bla", "location": "Gibraltar", "aforo": "2" }
    And header Accept = 'application/json'
    When method post
    Then status 200

    * def id = response.id
    * print 'created id is: ', id

    Given path id
    # When method get
    # Then status 200
    # And match response == { "name": "Test Event", "description": "bla bla bla", "location": "Gibraltar", "aforo": "2" }