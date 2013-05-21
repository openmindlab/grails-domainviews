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

  def static load(application){
    GroovyClassLoader classLoader = new GroovyClassLoader(DomainViewsBuilder.classLoader)
    def views = []
    def viewMap = [:]
    //application.mainContext.getResource('classpath:').file.eachFileMatch( ~/.*Views\.groovy/ ){f -> 
    //load all file from config ending in Views
    application.config.domainViewsConfig.viewsClasses.each{ f ->
      try {
        Class  domainsViews = classLoader.loadClass(f)
        //log.info "${f.name} found"
        domainsViews.metaClass.views = DomainViewsBuilder.&views
        views << domainsViews.newInstance().run()
      } catch (Exception ex) {
        log.error "Error loading ${f}. $ex"
      }
    }
    views.each{
      it.each{ domainName, domain ->
        if(!viewMap.containsKey(domainName)){
          viewMap[domainName] = domain
        }else {
          viewMap[domainName].views << domain.views
        }
      }
    }
    return viewMap
  }
}
