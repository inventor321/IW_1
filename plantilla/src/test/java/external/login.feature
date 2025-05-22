Feature: login en servidor

  @login_org
  Scenario: login correcto como org
    Given driver 'http://localhost:8080/login'
    * input('#username', 'ORG')
    * input('#password', 'aa')
    * click("button[id='loguear']")
    * waitForUrl(baseUrl + '/org/')
    Given driver 'http://localhost:8080/events/create'
    * input('#eventName', 'fieston')
    * input('#eventDescription', 'que bueno')
    * input('#eventLocation', 'aqui mismo')
    * input('#eventAforo', '2')
    * select('select[id=eventCategory]', 'CULTURA_Y_ARTE')
    * click("button[id='crear']")
    * delay(1000)
    * waitForUrl(baseUrl + '/events/198')
