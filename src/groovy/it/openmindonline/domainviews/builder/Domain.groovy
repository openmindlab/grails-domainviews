package it.openmindonline.domainviews.builder

import org.apache.log4j.Logger

class Domain{
  static log = Logger.getLogger(Domain.class)

  def views=[:]

  static make(Closure cl){
    expands new Domain(), cl
  }

  static expands(Domain domain, Closure cl){
    cl.delegate = domain
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl.call()
    return domain
  }

  def methodMissing(String method, args){
    log.info "view $method"
    views[method] = View.make(method,args[0])
  }

  def getALL(){
    new ViewAll()
  }
}