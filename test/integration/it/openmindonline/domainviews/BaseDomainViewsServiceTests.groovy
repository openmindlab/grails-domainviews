package it.openmindonline.domainviews

import it.openmindonline.domainviews.builder.*

abstract class BaseDomainViewsServiceTests {
  def domainViewsService
  def grailsApplication

  protected setViews(Closure cl){
    domainViewsService.grailsApplication.config.domainViews = DomainViewsBuilder.views(cl)
  }

  protected getViews(){
    domainViewsService.grailsApplication.config.domainViews
  }
}