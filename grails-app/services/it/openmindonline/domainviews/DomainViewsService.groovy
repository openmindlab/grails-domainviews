package it.openmindonline.domainviews

import grails.util.GrailsNameUtils
import it.openmindonline.domainviews.builder.*

class DomainViewsService {

    def transactional = false

    def grailsApplication

    private view(viewDef, obj) {
      if(obj != null){ 
        def map = [:]
        viewDef.properties.each{
          switch(it.class) {
            case String:
              def value = obj?."$it"
              map[it] = value instanceof Enum ? value.name() : value
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
        if(!viewDef) return obj
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
      if(clazz == Object) return 
      getViewsForDomainObject(clazz)?."$viewName" ?: getViewForDomainObject(clazz.superclass,viewName)
    }

    void normalize(clazz){
      def views = getViewsForDomainObject(clazz)
      def domainClass = grailsApplication.getDomainClass(clazz.name)
      views.findAll{_,view -> !view._normalized}.each{ viewName,view ->
        views[viewName] = normalizeView domainClass, view
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
      def prop = domainClass.getPropertyByName(propertyName)
      if (prop.association){
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
