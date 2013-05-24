package it.openmindonline.domainviews.builder

import static org.junit.Assert.*
import org.junit.*

class DomainViewsBuilderTests {

    def grailsApplication    

    @Test
    void load_multiple_files_ending_with_Views__DomainViews_groovy_and_BrandTestViews_groovy() {
        def views = DomainViewsBuilder.load(grailsApplication)
        assert views
        assert views.size()==2 //2 domains
    }

    @Test
    void mix_different_views_from_different_file_of_the_same_domain() {
        def views = DomainViewsBuilder.load(grailsApplication)
        assert views.brandTest.views
        assert views.brandTest.views.size()==2
        assert views.brandTest.views.containsKey('standard')
        assert views.brandTest.views.containsKey('standard2')
    }
}
