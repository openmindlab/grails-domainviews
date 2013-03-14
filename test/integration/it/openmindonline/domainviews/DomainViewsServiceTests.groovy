package it.openmindonline.domainviews

import it.openmindonline.domainviews.test.dataobjects.*
import it.openmindonline.domainviews.builder.*

import grails.test.mixin.*
import org.junit.*
import grails.buildtestdata.mixin.Build

import static org.junit.Assert.*
import grails.test.mixin.support.*

@TestFor(DomainViewsService)
@Build([ModelTest,BrandTest])
class DomainViewsServiceTests {

  def domainViewsService
  def grailsApplication

  @Test
  void 'simple direct property'(){
    setViews {
      modelTest{
        standard {
          name
        }
      }
    }
    def map = domainViewsService.applyView('standard', ModelTest.build(name:'Astra'))
    assert map
    assert map instanceof Map
    assert map.name
    assert map.name == 'Astra'
  }

  @Test
  void "complex property"(){
    setViews {
      modelTest {
        standard {
          name
          brandTest {
            name
          }
        }
      }
    }
    def modelTest = ModelTest.build(name:'Astra', brandTest: BrandTest.build(name:'Opel'))
    def map = domainViewsService.applyView('standard',modelTest)

    assert map.containsKey('name')
    assert map.name == 'Astra'
    assert map.containsKey('brandTest')
    assert map.brandTest instanceof Map
    assert map.brandTest.containsKey('name')
    assert map.brandTest.name == "Opel"
  }

  @Test
  void "all properties from complex property marked with ALL"(){
    setViews {
      modelTest {
        standard {
          name
          brandTest ALL
        }
      }
    }
    def modelTest = ModelTest.build(name:'Astra', brandTest: BrandTest.build(name:'Opel'))
    def map = domainViewsService.applyView('standard',modelTest)

    assert map.containsKey('brandTest')
    assert map.brandTest instanceof Map
    assert map.brandTest.size() == 2
    assert map.brandTest.containsKey('id')
    assert map.brandTest.containsKey('name')
    assert map.brandTest.id   == modelTest.brandTest.id
    assert map.brandTest.name == "Opel"
  }

  @Test
  void "complex property, get only id"(){
    setViews {
      modelTest {
        standard {
          name
          brandTest
        }
      }
    }
    def modelTest = ModelTest.build(name:'Astra', brandTest: BrandTest.build(name:'Opel'))
    def map = domainViewsService.applyView('standard',modelTest)
    assert map.brandTest
    assert map.brandTest.size()==1
    assert map.brandTest.id
    assert map.brandTest.id == modelTest.brandTest.id
  }

  @Test
  void "if no view match return orignal object"(){
    setViews {
      fantasyDomain {
        standard {
          name
          surname
          company
        }
      }
    }
    def modelTest = ModelTest.build(name:'Astra', brandTest: BrandTest.build(name:'Opel'))
    def obj = domainViewsService.applyView('standard',modelTest)
    assert obj instanceof ModelTest
    assert obj == modelTest
  }

  @Test
  void "set normalized flag"(){
    setViews {
      modelTest {
        standard{
          brandTest
        }
      }
    }
    domainViewsService.normalize(ModelTest)
    assert views.modelTest.views.standard._normalized
  }

  @Test
  void "normalize complex property, get only id"(){
    setViews {
      modelTest {
        standard{
          brandTest
        }
      }
    }

    domainViewsService.normalize(ModelTest)

    assert views.modelTest
    assert views.modelTest.views.standard
    assert views.modelTest.views.standard.properties
    assert views.modelTest.views.standard.properties.size() == 1
    assert views.modelTest.views.standard.properties[0] instanceof View
    def brandTest = views.modelTest.views.standard.properties[0]
    assert brandTest.properties
    assert brandTest.properties.size()==1
    assert brandTest.properties[0] == 'id'
  }

  @Test
  void "normalize complex property, take all properties"(){
    setViews {
      modelTest {
        standard {
          brandTest ALL
        }
      }
    }

    domainViewsService.normalize(ModelTest)

    def brandTest = views.modelTest.views.standard.properties[0]
    assert brandTest
    assert brandTest.properties
    assert brandTest.properties.size()==2
    assert brandTest.properties[0] == 'id'
    assert brandTest.properties[1] == 'name'
  }

  @Test
  void 'idempotent normalization'(){
    setViews {
      modelTest {
        standard {
          name
          brandTest {
            name
          }
        }
      }
    }

    domainViewsService.normalize(ModelTest)

    def standardView = views.modelTest.views.standard
    assert standardView.properties
    assert standardView.properties.size()==2
    assert standardView.properties[0] == 'name'
    assert standardView.properties[1] instanceof View
    assert standardView.properties[1].properties
    assert standardView.properties[1].properties.size() == 1
    assert standardView.properties[1].properties[0] == 'name'
  }

  @Test
  void "normalize multiple nested levels terminating with ALL"(){
    setViews {
      vehicleTest {
        standard {
          modelTest ALL
        }
      }
    }
    domainViewsService.normalize(VehicleTest)
    def vehicleTestView = views.vehicleTest.views.standard
    assert vehicleTestView
    assert vehicleTestView.properties
    assert vehicleTestView.properties.size() == 1
    def modelTestView   = vehicleTestView.properties[0]
    assert modelTestView
    assert modelTestView.properties
    assert modelTestView.properties.size() == 4
    assert modelTestView.properties.contains('id')
    assert modelTestView.properties.contains('name')
    assert modelTestView.properties.contains('modelVersion')
    def brandTestView   = modelTestView.properties.find{it instanceof View}
    assert brandTestView
    assert brandTestView._name =='brandTest'
    assert brandTestView.properties
    assert brandTestView.properties.size() == 1
    assert brandTestView.properties[0] == 'id'
  }

  @Test
  void "normalize multiple nested levels terminating with complex property"(){
    setViews {
      vehicleTest {
        standard {
          contractTest {
            data
          }
        }
      }
    }
    domainViewsService.normalize(VehicleTest)
    def vehicleTestView = views.vehicleTest.views.standard
    def contractView = vehicleTestView.properties[0]
    assert contractView
    assert contractView.properties
    assert contractView.properties.size()==1
    def contractData = contractView.properties[0]
    assert contractData
    assert contractData.properties
    assert contractData.properties.size()==1
    assert contractData.properties[0]=='id'
  }

  @Test
  void 'simple property with ALL should be interpreted as simple property'(){
    setViews {
      modelTest{
        standard{
          name ALL
        }
      }
    }
    domainViewsService.normalize(ModelTest)
    def modelTestView = views.modelTest.views.standard

    assert modelTestView
    assert modelTestView.properties
    assert modelTestView.properties.size() == 1
    assert modelTestView.properties[0]=='name'
  }

  @Test
  void "normalize all properties of the first level domain"(){
    setViews{
      modelTest {
        standard ALL
      }
    }

    domainViewsService.normalize(ModelTest)
    def modelTestView = views.modelTest.views.standard

    assert modelTestView
    assert modelTestView.properties
    assert modelTestView.properties.size() == 4
    
    def brandView = modelTestView.properties.find{it instanceof View}
    assert brandView
    assert brandView.properties
    assert brandView.properties.size()==1
    assert brandView.properties[0]=='id'
  }

  @Test
  void "normalize EXTENDS overriding properties with explict specified properties"(){
    setViews {
      modelTest {
        standard {
          EXTENDS
          brandTest {
            name
          }
        }
      }
    }

    domainViewsService.normalize(ModelTest)
    def modelTestView = views.modelTest.views.standard

    assert modelTestView
    assert modelTestView.properties
    assert modelTestView.properties.size()==4

    def brandView = modelTestView.properties.find{it instanceof View}
    assert brandView
    assert brandView.properties
    assert brandView.properties.size()==1
    assert brandView.properties[0]=='name'
  }

  ///          APPLY VIEW TESTS ------------------------
  @Test
  void 'view passed by thread local'(){
    setViews {
      modelTest{
        standard{
          name
        }
      }
    }
    domainViewsService.setView 'standard'
    def map = domainViewsService.applyView(ModelTest.build(name:'Astra'))
    assert map
    assert map instanceof Map
    assert map.name == 'Astra'
  }

  @Test
  void 'view passed by thread local, 2 threads'(){
    setViews {
      modelTest {
        standard {
          name
        }
        onlyId {
          id
        }
      }
    }
    domainViewsService.setView 'standard'
    def modelTest = ModelTest.build(name: 'Astra')
    Thread.start {
      domainViewsService.setView 'onlyId'
      def modelTestOnlyId = domainViewsService.applyView modelTest
      assert modelTestOnlyId
      assert modelTestOnlyId.size() == 1
      assert modelTestOnlyId.id == modelTest.id
    }
    def modelTestStandard = domainViewsService.applyView modelTest
    assert modelTestStandard
    assert modelTestStandard.size() == 1
    assert modelTestStandard.name == 'Astra'
  }

  @Test
  void 'unmatching view defined in thread local'(){
    setViews {
      modelTest {
        unmatching {
          name
        }
      }
    }
    domainViewsService.setView 'standard'
    def modelTest = ModelTest.build(name:'Astra')
    def obj = domainViewsService.applyView(modelTest)

    assert modelTest == obj
  }

  @Test
  void 'null view defined in thread local'(){
    setViews {
      modelTest {
        unmatching {
          name
        }
      }
    }
    domainViewsService.setView null
    def modelTest = ModelTest.build(name:'Astra')
    def obj = domainViewsService.applyView(modelTest)

    assert modelTest == obj
  }

  private setViews(Closure cl){
    domainViewsService.grailsApplication.config.domainViews = DomainViewsBuilder.views(cl)
  }

  private getViews(){
    domainViewsService.grailsApplication.config.domainViews
  }
}
