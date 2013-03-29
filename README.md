grails-domainsview
==================

Grails plugin that allows to define views via a custom DSL to convert Grails Domain Objects to maps.
 
* Plugin will load views from files ending with Views placed in the `grails-app/conf` directory
* domainViewsService injectable service
* properties ignored when a view is applied
    * services (matching `/.*Service/` )
    * injected `grailsApplication`
    * `version`
* handled domain properties
    * collection of domains 
    * simple property    
    * embedded domain objects
    * associations
    * enumerations
    * transient properties

##Basic view
```groovy
views{
  book {        // domain logic property name Book domain -> book
    standard {  // default view 
      title     // simple string property
    }
  }
}
```
apply view with 
```groovy
def map = domainViewsService.applyView('standard', new Book(
                                              isbn:'0-671-69267-4'
                                             ,title:"Dirk Gently's Holistic Detective Agency")

assert map instanceof Map
assert map == [title:"Dirk Gently's Holistic Detective Agency"]
```

##Load all properties from a domain
```groovy
views{
  book {        
    standard ALL  // special value
  }
}

def map = domainViewsService.applyView('standard', new Book(
                                              isbn:'0-671-69267-4'
                                             ,author: new Author(name:'Douglas Adams')
                                             ,title:"Dirk Gently's Holistic Detective Agency")
                                             
assert map == [title: "Dirk Gently's Holistic Detective Agency"
                  , id: 1
                  , isbn:'0-671-69267-4'
                  , author: [id: 2] ]
```
`ALL` load all properties of a domain object, if a property is an association the `id` is loaded
