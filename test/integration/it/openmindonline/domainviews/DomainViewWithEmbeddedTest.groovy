package it.openmindonline.domainviews

import it.openmindonline.domainviews.test.dataobjects.*
import it.openmindonline.domainviews.builder.*

import grails.test.mixin.*
import org.junit.*
import grails.buildtestdata.mixin.Build

import static org.junit.Assert.*
import grails.test.mixin.support.*

import grails.converters.XML

@TestFor(DomainViewsService)
@Build([DomainWithEmbeddedProperties])
class DomainViewWithEmbeddedTest extends BaseDomainViewsServiceTests {

  @Test
  void 'normalizing view of domain with embedded properties list all embedded properties, ALL properties from a domain'(){
    setViews{
      domainWithEmbeddedProperties {
        standard ALL
      }
    }

    domainViewsService.normalize(DomainWithEmbeddedProperties)
    def standardView = views.domainWithEmbeddedProperties.views.standard
    def embeddeView = standardView.properties.find{ it instanceof View }
    assert embeddeView.properties.size()==1
    assert embeddeView.properties.contains('embddProperty')
  }

  @Test
  void 'normalizing view of domain with embedded properties list all embedded properties, EXTENDS properties of a domain'(){
    setViews{
      domainWithEmbeddedProperties {
        standard {
          EXTENDS
        }
      }
    }

    domainViewsService.normalize(DomainWithEmbeddedProperties)
    def standardView = views.domainWithEmbeddedProperties.views.standard
    def embeddeView = standardView.properties.find{ it instanceof View }
    assert embeddeView.properties.size()==1
    assert embeddeView.properties.contains('embddProperty')
  }

  @Test
  void 'handling collection of domains with embedded properties'(){
    setViews{
      domainWithEmbeddedProperties{
        standard ALL
      }
    }

    domainViewsService.normalize(DomainWithEmbeddedProperties)
    
    def b = DomainWithEmbeddedProperties.build()
    b.property = new DomainWithEmbeddedProperties.EmbeddedDomainProperty()
    b.property.embddProperty = 'ola'

    def coll = domainViewsService.applyView('standard', [b,b,b])

    assert coll in Collection
    println coll
  }
}