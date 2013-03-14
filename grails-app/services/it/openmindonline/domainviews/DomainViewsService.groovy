package it.openmindonline.domainviews

import grails.util.GrailsNameUtils
import it.openmindonline.domainviews.builder.*

class DomainViewsService {

    def transactional = false

    private static final ThreadLocal viewThreadLocal = new ThreadLocal();

    def grailsApplication

    def setView(String viewName){
      viewThreadLocal.set(viewName)
    }

    def applyView(obj){
      String viewName = viewThreadLocal.get()
      viewThreadLocal.set(null)
      applyView(viewName, obj)
    }

    private view(viewDef, obj) {
      def map = [:]
      viewDef.properties.each{
        switch(it.class) {
          case String:
            map[it] = obj[it]
          break
          case View:
            map[it._name] = view(it,obj[it._name]);
          break
        }
      }
      return map
    }

    def applyView(String viewName, obj){
      normalize obj.class
      def viewDef = getViewsForDomainObject(obj.class)?."$viewName" 

      println "************"
      println obj.class
      println viewDef
      println getViewsForDomainObject(obj.class)
      println "************"

      if(!viewDef) return obj
      view(viewDef,obj)
    }

    def getViewsForDomainObject(clazz){
      def name = GrailsNameUtils.getPropertyNameRepresentation(clazz)
      grailsApplication.config.domainViews?."$name"?.views ?: [:]
      
    }

    void normalize(clazz){
      def views = getViewsForDomainObject(clazz)
      def domainClass = grailsApplication.getDomainClass(clazz.name)
      views.findAll{_,view -> !view._normalized}.each{ _,view ->
        normalizeView domainClass, view
      }
    }

    private normalizeView(domainClass, view){
      view.properties = view.properties.collect{property -> 
        normalizeProperty(domainClass, property)
      }
      view._normalized = true
      [view._name, view]
    }

    private normalizeProperty(domainClass, String propertyName){
      def prop = domainClass.getPropertyByName(propertyName)
      if (prop.association){
        new View(_name:propertyName, properties: ['id'])
      }else{
        propertyName
      }
    }

    private normalizeProperty(domainClass, ViewAll view){
        def prop = domainClass.getPropertyByName(view._name)
        if (prop.association){
          def newView = new View(_name: view._name)
          newView.properties = prop.referencedDomainClass.properties.findAll{it.name!='version'}*.name
          return normalizeView(prop.referencedDomainClass, newView)[1]
        }else{
          view._name
        }
    }

    private normalizeProperty(domainClass, View view){
      def prop = domainClass.getPropertyByName(view._name)
      def normalizedView = normalizeView(prop.referencedDomainClass, view)[1]
      return normalizedView
    }

}
