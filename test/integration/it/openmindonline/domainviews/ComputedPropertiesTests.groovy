package it.openmindonline.domainviews

import it.openmindonline.domainviews.test.dataobjects.*
import it.openmindonline.domainviews.builder.*

import grails.test.mixin.*
import org.junit.*
import grails.buildtestdata.mixin.Build

import static org.junit.Assert.*
import grails.test.mixin.support.*

@TestFor(DomainViewsService)
@Build([ModelTest])
class ComputedPropertiesTests extends BaseDomainViewsServiceTests {

  @Test
  void 'simple computed properties'(){
    setViews{
      modelTest{
        standard {
          _computed altName: { "alt_$name" }
        }
      }
    }

    def map = domainViewsService.applyView(ModelTest.build(name:'Astra'))
    assert map.altName == 'alt_Astra'
  }

  @Test
  void 'static computed properties'(){
    setViews{
      modelTest{
        standard {
          _computed value: { "value" }
        }
      }
    }

    def map = domainViewsService.applyView(ModelTest.build(name:'Astra'))
    assert map.value == 'value'
  }

  @Test
  void 'handling errors'(){
    setViews{
      modelTest{
        standard {
          _computed valueException: {throw new Exception()}
          _computed valueError:     {throw new Error()}
        }
      }
    }

    def map = domainViewsService.applyView(ModelTest.build(name:'Astra'))
    assert map.valueException == null
    assert map.valueError     == null
  }

  @Test
  void 'injecting mainContext as _ctx'(){
    setViews{
      modelTest{
        standard {
          _computed 'a_service': { _ctx.domainViewsService }
        }
      }
    }

    def map = domainViewsService.applyView(ModelTest.build(name:'Astra'))
    assert map.a_service
  }

  @Test
  void 'injecting config as _config'(){
    setViews{
      modelTest{
        standard {
          _computed 'a_config': { _config }
        }
      }
    }

    def map = domainViewsService.applyView(ModelTest.build(name:'Astra'))
    assert map.a_config
  }

  @Test
  void 'injecting the current date as _now'(){
    setViews{
      modelTest{
        standard {
          _computed 'a_date': { _now }
        }
      }
    }

    def map = domainViewsService.applyView(ModelTest.build(name:'Astra'))
    assert map.a_date
    assert map.a_date instanceof Date
  }

  @Test
  void 'working with normalization'(){
    setViews{
      modelTest{
        standard {
          _computed value: { "value" }
        }
      }
    }

    domainViewsService.normalize(ModelTest)
    def map = domainViewsService.applyView(ModelTest.build(name:'Astra'))
    assert map.value == 'value'
  }

  @Test
  void 'computed properties of nested associations'(){
    setViews{
      modelTest{
        standard {
          brandTest{
            _computed value: { "value$name" }
          }
        }
      }
    }

    domainViewsService.normalize(ModelTest)
    def map = domainViewsService.applyView(ModelTest.build(name:'Astra', brandTest: BrandTest.build(name:'Opel')))
    assert map.brandTest.value == 'valueOpel' 
  }

  @Test
  void 'computed properties with EXTENDS'(){
    setViews{
      modelTest{
        standard {
          EXTENDS
          _computed value: { "value" }
        }
      }
    }

    domainViewsService.normalize(ModelTest)
    def map = domainViewsService.applyView(ModelTest.build(name:'Astra'))
    assert map.containsKey('value')
    assert map.containsKey('id')
    assert map.containsKey('name')
  }

  @Test
  void 'computed properties with an id'(){
    setViews{
      modelTest{
        standard {
          name
          _computed value: { id }
        }
      }
    }

    domainViewsService.normalize(ModelTest)
    def map = domainViewsService.applyView(ModelTest.build(name:'Astra'))
    assert map.value
  }
}