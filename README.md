Grails Time-Travel
=================

[![Build Status](https://drone.io/github.com/Alotor/grails-timetravel/status.png)](https://drone.io/github.com/Alotor/grails-timetravel/latest)

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
    ...
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

# Jump Forward / Jump Backward
Once you're inside the travel context you can move the internal time clock advancing some time with
the jumpForward/jumpBackward method.

```groovy
void 'My awesome test'() {
    setup:
        travel(10.years.ago) {
            10.times {
                jumpForward 1.year
                testDomainService.insertDomainObject(name, value)
            }
        }

    when:
        ...

    then:
        ....
}
```

```groovy
void 'My awesome test'() {
    setup:
        travel(10.years.ago) {
            12.times {
                jumpBackward 1.month
                testDomainService.insertDomainObject(name, value)
            }
        }

    when:
        ...

    then:
        ....
}
```

Version info
------------
* 0.1 - 17/Nov/2013 - Initial version
* 0.2 - 17/Nov/2013 - Fixed problem in plugin descriptor
* 0.3 - 20/Nov/2013 - Solved problems with flush and listener collision
