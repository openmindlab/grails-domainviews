package it.openmindonline.domainviews.test.dataobjects

class ModelTest implements Comparable{
    
    BrandTest brandTest
    String name
    String modelVersion

    static constraints = {
      modelVersion nullable:true
    }

    int compareTo(obj) {
        name.compareTo(obj.name)
    }
}
