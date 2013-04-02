package it.openmindonline.domainviews.test.dataobjects

class DomainWithCollection {
  SortedSet sortedModels

  static hasMany = [
    models: ModelTest
    ,sortedModels: ModelTest
  ]

}
