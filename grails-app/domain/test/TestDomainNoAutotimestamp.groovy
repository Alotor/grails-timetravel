package test

class TestDomainNoAutotimestamp {
    String name
    Integer value

    Date dateCreated
    Date lastUpdated

    static mapping = {
        autoTimestamp false
    }

    static constraints = {
        dateCreated nullable: true
        lastUpdated nullable: true
    }
}
