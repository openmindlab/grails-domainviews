package it.openmindonline.domainviews.builder

import org.apache.commons.logging.LogFactory

class View{
  static log = LogFactory.getLog("it.openmindonline.domainviews.builder.View")
  
  def _name
  def _normalized = false
  def _toExtend   = false 
  def properties = []

  static make(String _name,Closure cl){
    View view = new View(_name:_name)
    cl.delegate = view
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl.call()
    return view
  }

  static make(String _name, ViewAll view){
    view._name = _name
    view
  }

  def methodMissing(String method, args){
    log.info "complex property $method"
    properties << View.make(method,args[0])
  }

  def propertyMissing(String property){
    log.info "property $property"
    properties << property
  }

  def getEXTENDS(){
    _toExtend = true
  }

  def getALL(){
    new ViewAll()
  }
}

class ViewAll extends View{

}

class ExtendsView extends View{
  View _parent
}