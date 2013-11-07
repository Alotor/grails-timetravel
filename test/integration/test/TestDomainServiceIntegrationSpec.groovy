package test

import grails.plugin.spock.IntegrationSpec

import java.util.Calendar
import org.grails.plugins.timetravel.TimeTravel

class TestDomainServiceIntegrationSpec extends IntegrationSpec {
    def testDomainService

    void 'Test save domain object'() {
        setup:
            def calendar = Calendar.getInstance()
            def curYear = calendar.get(Calendar.YEAR)

        when:
            def result = null
            use(TimeTravel) {
                def past = 10.years.ago
                testDomainService.insertDomainObject(name, value)

                def domain = TestDomain.findByName(name)
                domain.dateCreated = past
                domain.lastUpdated = past
                domain.save(flush: true)
            }
            result = TestDomain.findByName(name)

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
}
