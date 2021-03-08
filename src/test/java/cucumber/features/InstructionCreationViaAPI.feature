Feature: Instruction Creation Via Rest API

  <pre>
  This feature includes scenarios that covers successful and unsuccessful creation of an instruction via Rest API calls
  </pre>

  Scenario: Test the successful creation of instruction - Happy path
    Given generate an hmac token
    And generate a bearer token using hmac token
    When create an order via rest call
    And create an instruction via rest call

  Scenario: Test the Unsuccessful creation of instruction - Error path
    Given generate an hmac token
    And generate a bearer token using hmac token
    When create an order via rest call
    And create an instruction with invalid instruction ref via rest call