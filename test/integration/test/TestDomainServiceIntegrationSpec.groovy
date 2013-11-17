package test

import grails.plugin.spock.IntegrationSpec
import groovy.time.TimeCategory

import org.grails.plugins.timetravel.TimeTravel

@Mixin(TimeTravel)
class TestDomainServiceIntegrationSpec extends IntegrationSpec {
    def testDomainService

    def setupSpec() {
        Integer.mixin(TimeCategory)
    }

    void 'Test save domain object'() {
        when:
            travel(10.years.ago) {
                testDomainService.insertDomainObject(name, value)
            }
            def result = TestDomain.findByName(name)

        then:
            result != null
            result.dateCreated != null
            result.dateCreated.year == 10.years.ago.year
            result.lastUpdated != null
            result.lastUpdated.year == 10.years.ago.year

        where:
            name = "test"
            value = 10
    }

    void 'Test update domain object'() {
        setup:
            travel(10.years.ago) {
                testDomainService.insertDomainObject(name, value1)
            }

        when:
            travel(5.years.ago) {
                testDomainService.updateDomainObject(name, value2)
            }
            def result = TestDomain.findByName(name)

        then:
            result != null
            result.dateCreated != null
            result.dateCreated.year == 10.years.ago.year
            result.lastUpdated != null
            result.lastUpdated.year == 5.years.ago.year

        where:
            name = "test"
            value1 = 10
            value2 = 5
    }
}
