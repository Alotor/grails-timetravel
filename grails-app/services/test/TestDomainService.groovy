package test

class TestDomainService {

    public insertDomainObject(String name, Integer value) {
        new TestDomain(name: name, value: value).save()
    }

    public updateDomainObject(String name, Integer value) {
        def domain = TestDomain.findByName(name)
        domain.value = value
        domain.save()
    }
}
