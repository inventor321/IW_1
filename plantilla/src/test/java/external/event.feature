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
