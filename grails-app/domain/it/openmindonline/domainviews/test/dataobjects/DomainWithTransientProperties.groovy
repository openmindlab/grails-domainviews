package it.openmindonline.domainviews.test.dataobjects

class DomainWithTransientProperties {
  String name

  static transients = ['transientProperty']

  def getTransientProperty(){
    return new ModelTest(name:'modelName')
  }
}

