Feature: Event creation

    Background:
        Given url baseUrl
        Given path '/api/event'

    Scenario: Event creation

        * method GET
        * status 200
        * match $ == "Event created!"

    Scenario: