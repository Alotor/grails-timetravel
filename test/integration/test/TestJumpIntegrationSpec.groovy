package test

import grails.plugin.spock.IntegrationSpec
import groovy.time.TimeCategory

import static org.grails.plugins.timetravel.TimeTravel.*

class TestJumpIntegrationSpec extends IntegrationSpec {
    def testDomainService

    def setupSpec() {
        Integer.mixin(TimeCategory)
    }

    void 'Test save domain object and then jump'() {
        when:
            travel(10.year.ago) {
                testDomainService.insertDomainObject(name, value1)
                println ">> jumping"
                jumpForward 1.year
                println ">> jumping end"
                testDomainService.updateDomainObject(name, value2)
            }
            def result = TestDomain.findByName(name)

        then:
            result != null
            result.dateCreated != null
            result.dateCreated.year == 10.years.ago.year
            result.lastUpdated != null
            result.lastUpdated.year == 9.years.ago.year

        where:
            name = "test"
            value1 = 10
            value2 = 5
    }

    void 'Test save domain object and then jump'() {
        when:
            travel(10.year.ago) {
                testDomainService.insertDomainObject(name, value1)
                jumpBackward 1.year
                testDomainService.updateDomainObject(name, value2)
            }
            def result = TestDomain.findByName(name)

        then:
            result != null
            result.dateCreated != null
            result.dateCreated.year == 10.years.ago.year
            result.lastUpdated != null
            result.lastUpdated.year == 11.years.ago.year

        where:
            name = "test"
            value1 = 10
            value2 = 5
    }
}
