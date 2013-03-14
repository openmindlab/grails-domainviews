package it.openmindonline.domainviews

import it.openmindonline.domainviews.test.dataobjects.*
import it.openmindonline.domainviews.builder.*

import grails.test.mixin.*
import org.junit.*
import grails.buildtestdata.mixin.Build

import static org.junit.Assert.*
import grails.test.mixin.support.*

@TestFor(DomainViewsService)
@Build([Model,Brand])
class DomainViewsServiceTests {

  def domainViewsService
  def grailsApplication

  @Test
  void 'simple direct property'(){
    setViews {
      model{
        standard {
          name
        }
      }
    }
    def map = domainViewsService.applyView('standard', Model.build(name:'Astra'))
    assert map
    assert map instanceof Map
    assert map.name
    assert map.name == 'Astra'
  }

  @Test
  void "complex property"(){
    setViews {
      model {
        standard {
          name
          brand {
            name
          }
        }
      }
    }
    def model = Model.build(name:'Astra', brand: Brand.build(name:'Opel'))
    def map = domainViewsService.applyView('standard',model)

    assert map.containsKey('name')
    assert map.name == 'Astra'
    assert map.containsKey('brand')
    assert map.brand instanceof Map
    assert map.brand.containsKey('name')
    assert map.brand.name == "Opel"
  }

  @Test
  void "all properties from complex property marked with ALL"(){
    setViews {
      model {
        standard {
          name
          brand ALL
        }
      }
    }
    def model = Model.build(name:'Astra', brand: Brand.build(name:'Opel'))
    def map = domainViewsService.applyView('standard',model)

    assert map.containsKey('brand')
    assert map.brand instanceof Map
    assert map.brand.size() == 2
    assert map.brand.containsKey('id')
    assert map.brand.containsKey('name')
    assert map.brand.id   == model.brand.id
    assert map.brand.name == "Opel"
  }

  @Test
  void "complex property, get only id"(){
    setViews {
      model {
        standard {
          name
          brand
        }
      }
    }
    def model = Model.build(name:'Astra', brand: Brand.build(name:'Opel'))
    def map = domainViewsService.applyView('standard',model)
    assert map.brand
    assert map.brand.size()==1
    assert map.brand.id
    assert map.brand.id == model.brand.id
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
    def model = Model.build(name:'Astra', brand: Brand.build(name:'Opel'))
    def obj = domainViewsService.applyView('standard',model)
    assert obj instanceof Model
    assert obj == model
  }

  @Test
  void "set normalized flag"(){
    setViews {
      model {
        standard{
          brand
        }
      }
    }
    domainViewsService.normalize(Model)
    assert views.model.views.standard._normalized
  }

  @Test
  void "normalize complex property, get only id"(){
    setViews {
      model {
        standard{
          brand
        }
      }
    }

    domainViewsService.normalize(Model)

    assert views.model
    assert views.model.views.standard
    assert views.model.views.standard.properties
    assert views.model.views.standard.properties.size() == 1
    assert views.model.views.standard.properties[0] instanceof View
    def brand = views.model.views.standard.properties[0]
    assert brand.properties
    assert brand.properties.size()==1
    assert brand.properties[0] == 'id'
  }

  @Test
  void "normalize complex property, take all properties"(){
    setViews {
      model {
        standard {
          brand ALL
        }
      }
    }

    domainViewsService.normalize(Model)

    def brand = views.model.views.standard.properties[0]
    assert brand
    assert brand.properties
    assert brand.properties.size()==2
    assert brand.properties[0] == 'id'
    assert brand.properties[1] == 'name'
  }

  @Test
  void 'idempotent normalization'(){
    setViews {
      model {
        standard {
          name
          brand {
            name
          }
        }
      }
    }

    domainViewsService.normalize(Model)

    def standardView = views.model.views.standard
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
      vehicle {
        standard {
          model ALL
        }
      }
    }
    domainViewsService.normalize(Vehicle)
    def vehicleView = views.vehicle.views.standard
    assert vehicleView
    assert vehicleView.properties
    assert vehicleView.properties.size() == 1
    def modelView   = vehicleView.properties[0]
    assert modelView
    assert modelView.properties
    assert modelView.properties.size() == 4
    assert modelView.properties.contains('id')
    assert modelView.properties.contains('name')
    assert modelView.properties.contains('modelVersion')
    def brandView   = modelView.properties.find{it instanceof View}
    assert brandView
    assert brandView._name =='brand'
    assert brandView.properties
    assert brandView.properties.size() == 1
    assert brandView.properties[0] == 'id'
  }

  @Test
  void "normalize multiple nested levels terminating with complex property"(){
    setViews {
      vehicle {
        standard {
          contract {
            data
          }
        }
      }
    }
    domainViewsService.normalize(Vehicle)
    def vehicleView = views.vehicle.views.standard
    def contractView = vehicleView.properties[0]
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
      model{
        standard{
          name ALL
        }
      }
    }
    domainViewsService.normalize(Model)
    def modelView = views.model.views.standard

    assert modelView
    assert modelView.properties
    assert modelView.properties.size() == 1
    assert modelView.properties[0]=='name'
  }

  @Test
  void 'view passed by thread local'(){
    setViews {
      model{
        standard{
          name
        }
      }
    }
    domainViewsService.setView 'standard'
    def map = domainViewsService.applyView(Model.build(name:'Astra'))
    assert map
    assert map instanceof Map
    assert map.name == 'Astra'
  }

  @Test
  void 'view passed by thread local, 2 threads'(){
    setViews {
      model {
        standard {
          name
        }
        onlyId {
          id
        }
      }
    }
    domainViewsService.setView 'standard'
    def model = Model.build(name: 'Astra')
    Thread.start {
      domainViewsService.setView 'onlyId'
      def modelOnlyId = domainViewsService.applyView model
      assert modelOnlyId
      assert modelOnlyId.size() == 1
      assert modelOnlyId.id == model.id
    }
    def modelStandard = domainViewsService.applyView model
    assert modelStandard
    assert modelStandard.size() == 1
    assert modelStandard.name == 'Astra'
  }

  @Test
  void 'unmatching view defined in thread local'(){
    setViews {
      model {
        unmatching {
          name
        }
      }
    }
    domainViewsService.setView 'standard'
    def model = Model.build(name:'Astra')
    def obj = domainViewsService.applyView(model)

    assert model == obj
  }

  @Test
  void 'null view defined in thread local'(){
    setViews {
      model {
        unmatching {
          name
        }
      }
    }
    domainViewsService.setView null
    def model = Model.build(name:'Astra')
    def obj = domainViewsService.applyView(model)

    assert model == obj
  }

  private setViews(Closure cl){
    domainViewsService.grailsApplication.config.domainViews = DomainViewsBuilder.views(cl)
  }

  private getViews(){
    domainViewsService.grailsApplication.config.domainViews
  }
}
