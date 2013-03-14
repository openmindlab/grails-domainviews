package it.openmindonline.domainviews

import it.openmindonline.domainviews.builder.DomainViewsBuilder
import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(DomainViewsService)
class DomainViewsServiceTests {
  def domainViewsService

  @Test
  void "get view for domainObject"(){
    setViews {
      testClass {
        standard {
          name
          brand
        }
      }
    }
    def objectViews = domainViewsService.getViewsForDomainObject(TestClass)
    assert objectViews
    assert objectViews.size() == 1
    assert objectViews.standard
    assert objectViews.standard.properties
    assert objectViews.standard.properties.size() == 2
  }

  @Test
  void "get views for domainObject, multiple view definitions"(){
    setViews {
      testClass {
        standard {
          name
          brand
        }
        test {
          name
        }
      }
    }
    def objectViews = domainViewsService.getViewsForDomainObject(TestClass)
    assert objectViews
    assert objectViews instanceof Map
    assert objectViews.size() == 2
  }

  @Test 
  void "get views for domain object, inexistent domain definition should return empty map"(){
    setViews {
      testClass {
        standard {
          name
          brand
        }
      }
    }
    def objectViews = domainViewsService.getViewsForDomainObject(TestClass2)
    assert objectViews instanceof Map
    assert objectViews.size() == 0
  }

  @Test
  void "get views for domain object, multiple domain views definitions"(){
    setViews {
      testClass {
        standard {
          name
          brand
        }
      }
      testClass {
        test {
          name
          brand {
            name
          }
        }
      }
    }
    def objectViews = domainViewsService.getViewsForDomainObject(TestClass)

    assert objectViews
    assert objectViews instanceof Map
    assert objectViews.size()==2
    assert objectViews.standard._name == "standard"
    assert objectViews.standard.properties.size()==2
    assert objectViews.test._name == "test"
    assert objectViews.test.properties.size()==2
  }

  void setUp() {
    domainViewsService = new DomainViewsService()
    domainViewsService.grailsApplication = [config:[:]]
  }

  void tearDown() {
  }
  
  private setViews(Closure cl){
    domainViewsService.grailsApplication.config.domainViews = DomainViewsBuilder.views(cl)
  }

  private getViews(){
    domainViewsService.grailsApplication.config.domainViews 
  }
}

class TestClass{}
class TestClass2{}