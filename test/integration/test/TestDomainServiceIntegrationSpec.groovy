package test

import grails.plugin.spock.IntegrationSpec

import net.kaleidos.timetravel.TimeTravel
import java.util.Calendar

class TestDomainServiceIntegrationSpec extends IntegrationSpec {
    def testDomainService

    void 'Test save domain object'() {
        setup:
            def calendar = Calendar.getInstance()
            def curYear = calendar.get(Calendar.YEAR)

        when:
            use(TimeTravel) {
                travel 10.years.ago
                def result = testDomainService.insertDomainObject(name, value)
            }



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
