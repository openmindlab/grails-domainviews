package it.openmindonline.domainviews.test.dataobjects

class VehicleTest {
  ModelTest modelTest
  ContractTest contractTest
  static constraints = {
    contractTest nullable: true
  }
}
