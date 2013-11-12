grails-timetravel
=================

Grails plugin to mock and test data as if created on a specific time.

## WARNING: Time-travel is dangerous
This library manipulates the space-time continium so it's not advised to use it on production code.
The code was meant to be used in testing environments only.

## Recommended use
We recommend to agregate the TimeCategory Mixin on the setup to ease the use of the library.

```groovy
class TestDomainServiceIntegrationSpec extends IntegrationSpec {
    def setupSpec() {
        Integer.mixin(TimeCategory)
    }
```

Then you have to add the Mixin to aggregate the time-travelling functions

```groovy
@Mixin(TimeTravel)
class TestDomainServiceIntegrationSpec extends IntegrationSpec {
    ...
}
```

And you are ready to travel in time. Select your date and make the jump:

```groovy
    void 'My awesome test'() {
        setup:
            travel(10.years.ago) {
                testDomainService.insertDomainObject(name, value)
            }

        when:
            ...

        then:
            ....
    }
```
