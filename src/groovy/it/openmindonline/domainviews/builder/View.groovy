package it.openmindonline.domainviews.builder

import org.apache.commons.logging.LogFactory

class View{
  static log = LogFactory.getLog("it.openmindonline.domainviews.builder.View")
  
  def _name
  def _normalized = false
  def properties = []

  static make(String _name,Closure cl){
    View view = new View(_name:_name)
    cl.delegate = view
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl.call()
    return view
  }

  static make(String _name, String whichProperties){
    switch(whichProperties){
      case ALL:
        return new ViewAll(_name:_name)
    }
  }

  def methodMissing(String method, args){
    log.info "complex property $method"
    properties << View.make(method,args[0])
  }

  def propertyMissing(String property){
    log.info "property $property"
    properties << property
  }

  static ALL = "all"
}

class ViewAll extends View{

}