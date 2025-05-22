Feature: Event creation and registration

    Background:
    * url baseUrl
  Scenario: get login page, capture csrf, send login

    # csrf

    * path 'login'
    * form field username = 'ORG'
    * form field password = 'aa'
    * method post
    * status 200

  Scenario: get create event page, create event

    # csrf

    * path 'events/create'
    * form field name = 'Quedada en el parque'
    * form field description = 'que bien que bien'
    * form field location = 'aqui'
    * form field aforo = 2
    * method post
    * status 200
