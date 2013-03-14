package it.openmindonline.domainviews.test.dataobjects

class ModelTest {
    
    BrandTest brandTest
    String name
    String modelVersion

    static constraints = {
      modelVersion nullable:true
    }
}
