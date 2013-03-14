package it.openmindonline.domainviews.test.dataobjects

class Model {
    
    Brand brand
    String name
    String modelVersion

    static constraints = {
      modelVersion nullable:true
    }

    String toString(){
      "${name}"
    }
}
