package it.openmindonline.domainviews.builder

import org.apache.commons.logging.LogFactory

class DomainViewsBuilder{

  static log = LogFactory.getLog("it.openmindonline.domainviews.builder.DomainViewsBuilder")

  def domains = [:]

  def static views(Closure cl){
    log.info "Resource view found"
    DomainViewsBuilder resource = new DomainViewsBuilder()
    cl.delegate = resource
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl.call()
  }

  def methodMissing(String method, args){
    log.info "domain: $method"
    if(!domains.containsKey(method) ){
      def domain = Domain.make(args[0])
      domains[method] = domain
    }else{
      Domain.expands(domains[method],args[0])
    }
    return domains
  }

  def static load(){
    GroovyClassLoader classLoader = new GroovyClassLoader(DomainViewsBuilder.classLoader)
    Class domainsViews
    try {
      domainsViews = classLoader.loadClass('DomainsViews')
      log.info "DomainsViews.groovy found"
      domainsViews.metaClass.views = DomainViewsBuilder.&views
      domainsViews.newInstance().run()
    } catch (ClassNotFoundException ex) {
      log.warn "DomainsViews.groovy not found"
    }

  }
}
