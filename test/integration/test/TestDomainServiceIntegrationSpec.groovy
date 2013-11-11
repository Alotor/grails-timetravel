package test

import grails.plugin.spock.IntegrationSpec

import java.util.Calendar
import org.grails.plugins.timetravel.TimeTravel
import groovy.time.TimeCategory

@Mixin(TimeTravel)
class TestDomainServiceIntegrationSpec extends IntegrationSpec {
    def testDomainService

    def setupSpec() {
        Integer.mixin(TimeCategory)
    }

    void 'Test save domain object'() {
        setup:
            def calendar = Calendar.getInstance()
            def curYear = calendar.get(Calendar.YEAR)

        when:
            travel(10.years.ago) {
                testDomainService.insertDomainObject(name, value)
            }
            def result = TestDomain.findByName(name)

        then:
            result != null
            result.dateCreated != null
            result.dateCreated.year == curYear -1900 -10
            result.lastUpdated != null
            result.lastUpdated.year == curYear -1900 -10

        where:
            name = "test"
            value = 10
    }

    void 'Test update domain object'() {
        setup:
            def calendar = Calendar.getInstance()
            def curYear = calendar.get(Calendar.YEAR)

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
            result.dateCreated.year == curYear -1900 -10
            result.lastUpdated != null
            result.lastUpdated.year == curYear -1900 -5

        where:
            name = "test"
            value1 = 10
            value2 = 5
    }
}
