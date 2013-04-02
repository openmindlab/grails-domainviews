package it.openmindonline.domainviews

import it.openmindonline.domainviews.test.dataobjects.*
import it.openmindonline.domainviews.builder.*

import grails.test.mixin.*
import org.junit.*
import grails.buildtestdata.mixin.Build

import static org.junit.Assert.*
import grails.test.mixin.support.*

@TestFor(DomainViewsService)
@Build([ModelTest,DomainWithCollection])
class DomainWithCollectionTests extends BaseDomainViewsServiceTests {

  def obj

  void setUp() {
    obj = DomainWithCollection.build(models:[ModelTest.build(name:'modelA'),ModelTest.build(name:'modelB')])
  }

  @Test
  void 'test domain with collections'(){
    setViews{
        domainWithCollection{
            standard{
                models
            }
        }
    }
    domainViewsService.normalize(DomainWithCollection)
    def map = domainViewsService.applyView('standard', obj)

    assert map
    assert map.models
    assert map.models.size()==2
    assert map.models[0] instanceof Map
  }

  @Test
  void 'collections of domains  with extracted ALL properties'(){
    setViews{
        domainWithCollection{
            standard{
                models ALL
            }
        }
    }
    domainViewsService.normalize(DomainWithCollection)
    def map = domainViewsService.applyView('standard', obj)

    assert map.models*.containsKey('id').every()
    assert map.models*.containsKey('name').every()
    assert map.models*.containsKey('modelVersion').every()
    assert map.models*.containsKey('brandTest').every()
  }

  @Test
  void 'collections of domains with explicited property'(){
    setViews{
        domainWithCollection{
            standard{
                models{
                    name
                }
            }
        }
    }
    domainViewsService.normalize(DomainWithCollection)
    def map = domainViewsService.applyView('standard', obj)

    assert map.models*.containsKey('name').every()
    assert map.models*.name.sort()==['modelA','modelB']
  }

  @Test
  void 'if collection is Sorted output a list witth correct order '(){
    setViews{
        domainWithCollection{
            standard{
                sortedModels{
                    name
                }
            }
        }
    }
    domainViewsService.normalize(DomainWithCollection)
    
    def obj2 = DomainWithCollection.build(sortedModels:new TreeSet([
         ModelTest.build(name:'modelD')
        ,ModelTest.build(name:'modelB')
        ,ModelTest.build(name:'modelA')
        ,ModelTest.build(name:'modelC')
    ]))

    def map = domainViewsService.applyView('standard', obj2)

    assert map.sortedModels*.name == ['modelA','modelB','modelC','modelD']
  }
}
