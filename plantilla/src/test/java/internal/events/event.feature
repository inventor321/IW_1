Feature: sample karate test script
  for help, see: https://github.com/intuit/karate/wiki/IDE-Support

  Background:
    * url 'https://jsonplaceholder.typicode.com'

  Scenario: get all events and then get the first event by id
    Given path 'events'
    When method get
    Then status 200

    * def first = response[0]

    Given path 'events', first.id
    When method get
    Then status 200

  Scenario: create an event and then get it by id
    * def event =
      """
      {
        "name": "Test Event",
        "description": "bla bla bla",
        "location": "Gibraltar",
      }
      """

    Given url 'https://jsonplaceholder.typicode.com/events/create'
    And request event
    When method post
    Then status 201

    * def id = response.id
    * print 'created id is: ', id

    Given path id
    # When method get
    # Then status 200
    # And match response contains event