package it.openmindonline.domainviews.builder

import static org.junit.Assert.*

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class DomainViewsBuilderTests {
  @Test
  void "simple configuration"(){
    def views = DomainViewsBuilder.views{
      model{
        test{
          id
          name
        }
      }
    }
    assert views
    assert views.model
    assert views.model.views
    assert views.model.views.test
    assert views.model.views.test.properties
  }

  @Test
  void "complex explicit properties"(){
    def views = DomainViewsBuilder.views{
      model {
        test {
          name
          brand {
            name
          }
        }
      }
    }
    def props = views.model.views.test.properties
    assert props.size() == 2
    assert props[0]=="name"
    assert props[1] instanceof View
    assert props[1]._name=="brand"
    assert props[1].properties
    assert props[1].properties.size()==1
    assert props[1].properties[0]=="name"
  }

  @Test
  void "complex property without argument"(){
    def views = DomainViewsBuilder.views{
      model {
        test {
          name
          brand
        }
      }
    }
    def props = views.model.views.test.properties
    assert props.size()==2
    assert props[0] == 'name'
    assert props[1] == 'brand'
  }

  @Test
  void "complex property with ALL as argument"(){
    def views = DomainViewsBuilder.views{
      model {
        test {
          name
          brand ALL
        }
      }
    }
    def props = views.model.views.test.properties
    assert props.size() == 2
    assert props[1]._name == 'brand'
    assert props[1] instanceof ViewAll
  }

  @Test
  void "multiple domain definition"(){
    def views = DomainViewsBuilder.views{
      model {
        test {
          name
          brand ALL
        }
      }
      model2 {
        test {
          name
        }
      }
    }
    assert views.model
    assert views.model.views.test
    assert views.model.views.test.properties
    assert views.model2
    assert views.model2.views.test
    assert views.model2.views.test.properties
  }

  @Test
  void "multiple views"(){
    def views = DomainViewsBuilder.views{
      model {
        test {
          name
        }
        test2 {
          name
        }
      }
    }
    assert views.model.views.test
    assert views.model.views.test2
    assert views.model.views.test.properties
    assert views.model.views.test2.properties
  }

  @Test
  void "multiple domains views definition"(){
    def views = DomainViewsBuilder.views{
      model {
        test {
          name
        }
      }
      model {
        test2 {
          surname
        }
      }
    }

    assert views.model
    assert views.model.views.test
    assert views.model.views.test.properties
    assert views.model.views.test2
    assert views.model.views.test2.properties
  }

  @Test
  void "all properties of the first level domain"(){
    def views = DomainViewsBuilder.views{
      model {
        standard ALL
      }
    }

    assert views
    assert views.model
    assert views.model.views.standard instanceof ViewAll
  }

  @Test
  void "all properties with extension"(){
    def views = DomainViewsBuilder.views{
      model {
        standard {
          EXTENDS
          brand {
            name
          }
        }
      }
    }

    assert views
    assert views.model
    assert views.model.views.standard
    assert views.model.views.standard instanceof View
    assert views.model.views.standard.properties
    assert views.model.views.standard.properties.size()==1
    assert views.model.views.standard._toExtend

  }
}
