Grails Time-Travel
=================

Grails plugin to mock and test data as if created on a specific date or time.

## WARNING: Time-travel is dangerous
The conde in this plugin anipulates the space-time continuum so it's not advised to use it on your
production code. The plugin was developed with testing environment in mind so be cautious.

## Recommended use
We recommend to use the groovy.time.TimeCategory mIxin on the setup to have a more idiomatic use of
the plugin.

```groovy
class TestDomainServiceIntegrationSpec extends IntegrationSpec {
    def setupSpec() {
        Integer.mixin(groovy.time.TimeCategory)
    }
```

Then you have to add the TimeTravel mixin to add the time manipulation functions.

```groovy
@Mixin(org.grails.plugins.timetravel.TimeTravel)
class TestDomainServiceIntegrationSpec extends IntegrationSpec {
    ...
}
```

## Functions
# Travel
All the domain objects saved inside the closure code (that have timestamping enabled) will be saved
with the creationDate/lastUpdated as the specified time.

When you're ready to travel in time just select your date and make the jump:

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

# TODO
* Repeat the closure code in several times or in a range of time
