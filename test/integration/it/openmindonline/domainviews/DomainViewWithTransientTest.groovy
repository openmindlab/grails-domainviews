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
class DomainViewWithTransientTest extends BaseDomainViewsServiceTests {

  @Test
  void 'complex transient properties'(){
    setViews{
      domainWithTransientProperties {
        standard{
          transientProperty {
            name
          }
        }
      }
    }

    domainViewsService.normalize(DomainWithTransientProperties)
    def standardView = views.domainWithTransientProperties.views.standard
    def transientView = standardView.properties.find{ it instanceof View }
    assert transientView.properties.size()==1
    assert transientView.properties.contains('name')
  }

  @Test
  void 'applying view to complex transient properties'(){
    setViews{
      domainWithTransientProperties {
        standard{
          transientProperty {
            name
          }
        }
      }
    }

    def obj = new DomainWithTransientProperties(name:'domainWithTransientProperty')
    domainViewsService.normalize(DomainWithTransientProperties)
    def view = domainViewsService.applyView('standard',obj)

    assert view.transientProperty.name == 'modelName'
  }  
}