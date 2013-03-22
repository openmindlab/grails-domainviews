package it.openmindonline.domainviews.test.dataobjects

class DomainWithEmbeddedProperties {
    EmbeddedDomainProperty property
    static embedded = ['property']
    static constraints = {
      property nullable: true
    }
}

class EmbeddedDomainProperty {
    String embddProperty
}
