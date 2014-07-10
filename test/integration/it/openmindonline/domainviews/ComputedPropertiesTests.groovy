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

    def map = domainViewsService.applyView('standard', ModelTest.build(name:'Astra'))
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

    def map = domainViewsService.applyView('standard', ModelTest.build(name:'Astra'))
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

    def map = domainViewsService.applyView('standard', ModelTest.build(name:'Astra'))
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

    def map = domainViewsService.applyView('standard', ModelTest.build(name:'Astra'))
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

    def map = domainViewsService.applyView('standard', ModelTest.build(name:'Astra'))
    assert map.a_config
  }
}