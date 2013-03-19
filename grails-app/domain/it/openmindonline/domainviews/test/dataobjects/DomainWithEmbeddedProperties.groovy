package it.openmindonline.domainviews.test.dataobjects

class DomainWithEmbeddedProperties {
    EmbeddedDomainProperty property
    static embedded = ['property']
}

class EmbeddedDomainProperty {
    String embddProperty
}
