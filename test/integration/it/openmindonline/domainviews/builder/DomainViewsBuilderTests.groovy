package it.openmindonline.domainviews.builder

import static org.junit.Assert.*
import org.junit.*

class DomainViewsBuilderTests {

    @Test
    void 'load multiple files ending with Views, DomainViews.groovy and BrandTestViews.groovy'() {
        def views = DomainViewsBuilder.load()
        assert views
        assert views.size()==2 //2 domains
    }

    @Test
    void 'mix different views from different file of the same domain'() {
        def views = DomainViewsBuilder.load()
        assert views.brandTest.views
        assert views.brandTest.views.size()==2
        assert views.brandTest.views.containsKey('standard')
        assert views.brandTest.views.containsKey('standard2')
    }
}
