package it.openmindonline.domainviews

import grails.util.GrailsNameUtils
import it.openmindonline.domainviews.builder.*
import org.apache.log4j.Logger
import groovy.lang.Binding

class DomainViewsService {
    static log = Logger.getLogger(DomainViewsService.class)
    
    def transactional = false
    def grailsApplication
    def binding

    private view(viewDef, obj) {
      binding = [_ctx: grailsApplication.mainContext, _config: grailsApplication.config]
      if(obj != null){ 
        def map = [:]
        viewDef.properties.each{
          switch(it) {
            case String:
              def value = obj.properties.containsKey(it) ? obj?."$it" : null
              map[it] = value instanceof Enum ? value.name() : value
            break
            case ComputedView:
              try{
                def rehydratedCl = it.cl.rehydrate(binding << obj.properties, this, it.cl)
                map[it._name] = rehydratedCl.call()
              }catch(Throwable e){
                log.error("Catched throwable generated on view ${viewDef._name} by computed property ${it._name} for obj $obj.",e)
              }
            break
            case View:
              def value = obj?."${it._name}"
              map[it._name] = view(it,value);
            break
          }
        }
        return map
      }
    }

    private view(viewDef, Collection collection){
      collection?.collect{
        view(viewDef,it)
      }
    }

    def filterProperties = {
      it.name!='version' && !(it.name =~ /.*Service/) &&  it.name!='grailsApplication'
    }

    def applyView(obj){
      applyView('standard',obj)
    }

    def applyView(String viewName, obj){
      if (obj != null) {
        def viewDef = getViewForDomainObject(obj.class,viewName)
        if(!viewDef){
          log.warn "View ${viewName} is not defined for obj ${obj} of class ${obj.class}"
          return obj
        }
        view(viewDef,obj)
      }
    }

    def applyView(String viewName, Collection collection){
      collection.collect{
        applyView(viewName, it)
      }
    }

    def getViewsForDomainObject(clazz){
      def name = !clazz ? false : GrailsNameUtils.getPropertyNameRepresentation(clazz)
      name ? grailsApplication.config.domainViews?."$name"?.views ?: [:] : [:]
    }

    def getViewForDomainObject(clazz, viewName){
      if(clazz == Object || clazz == null) return 
      getViewsForDomainObject(clazz)?."$viewName" ?: getViewForDomainObject(clazz?.superclass,viewName)
    }

    void normalize(clazz){
      def views = getViewsForDomainObject(clazz)
      def domainClass = grailsApplication.getDomainClass(clazz.name)
      views.findAll{_,view -> !view._normalized}.each{ viewName,view ->
        views[viewName] = normalizeView domainClass, view
        log.info "${clazz?.name}#$viewName"
      }
    }

    private normalizeView(domainClass, View view){
      def properties = view._toExtend ? 
        domainClass.properties
          .findAll(filterProperties)*.name.collect{ domainProperty ->
            view.properties.find{
              viewProperty -> viewProperty._name == domainProperty
            } ?: domainProperty
          } : view.properties

      view.properties = properties.collect{property -> 
        normalizeProperty(domainClass, property)
      }
      view._normalized = true
      view
    }

    private normalizeView(domainClass, ViewAll view){
      def newView = new View(_name:view._name)
      newView.properties = domainClass.properties.findAll(filterProperties)*.name
      normalizeView domainClass, newView
    }

    private normalizeProperty(domainClass, String propertyName){
      def prop = domainClass?.hasPersistentProperty(propertyName) ? domainClass.getPropertyByName(propertyName) : null
      if (prop?.association){
        new View(
            _name     : propertyName
          , properties: !prop.embedded ? ['id'] :
              prop.component.properties.findAll(filterProperties).findAll({it.name!='id'})*.name  )
      }else{
        propertyName
      }
    }

    private normalizeProperty(domainClass, ViewAll view){
        def prop = domainClass.getPropertyByName(view._name)
        if (prop.association){
          def newView = new View(_name: view._name)
          newView.properties = prop.referencedDomainClass.properties.findAll(filterProperties)*.name
          normalizeView(prop.referencedDomainClass, newView)
        }else{
          view._name
        }
    }

    private normalizeProperty(domainClass, View view){
      def prop = domainClass.getPropertyByName(view._name)
      normalizeView(prop.referencedDomainClass, view)
    }

}
