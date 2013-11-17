package test

class TestDomainNoAutotimestampService {
    static transactional = true

    public insertDomainObject(String name, Integer value) {
        def domain = new TestDomainNoAutotimestamp(name: name, value: value)
        domain.save()
    }

    public updateDomainObject(String name, Integer value) {
        def domain = TestDomainNoAutotimestamp.findByName(name)
        domain.value = value
        domain.save()
    }
}
