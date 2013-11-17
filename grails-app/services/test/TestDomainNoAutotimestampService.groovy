package test

class TestDomainNoAutotimestampService {

    public insertDomainObject(String name, Integer value) {
        new TestDomainNoAutotimestamp(name: name, value: value).save()
    }

    public updateDomainObject(String name, Integer value) {
        def domain = TestDomainNoAutotimestamp.findByName(name)
        domain.value = value
        domain.save()
    }
}
