import it.openmindonline.domainviews.builder.*
import it.openmindonline.domainviews.DomainViewsService

class DomainViewsGrailsPlugin {
    // the plugin version
    def version = "0.4.6"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.2 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
        ,"grails-app/domain/it/openmindonline/domainviews/test/dataobjects/*.groovy"
        ,"grails-app/services/it/openmindonline/domainviews/test/dataobjects/*groovy"
        ,"grails-app/conf/*"
    ]

    // TODO Fill in these fields
    def title = "Domain Views Plugin" // Headline display name of the plugin
    def author = "Carlo Colombo"
    def authorEmail = "carlo.colombo@openmindonline.it"
    def description = '''\
Grails plugin that allows to define views via a custom DSL to convert beans to maps
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/domain-views"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [ name: "Openmind", url: "http://www.openmindlab.com/" ]

    // Any additional developers beyond the author specified above.
    def developers = [
        [ name: "Manuel Molaschi", email: "manuel.molaschi@openmindonline.it" ]
    ]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "GITHUB", url: "https://github.com/openmindlab/grails-domainviews/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/openmindlab/grails-domainviews" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def loadAndNormalize(application){
        def views = DomainViewsBuilder.load(application)
        if (application.config.domainViewsConfig.defaultView){
            application.getArtefacts("Domain")
                .findAll{
                    application.config.domainViewsConfig.defaultView.packages.any{ packageName ->
                        it.clazz.package.toString().contains(packageName)
                    }
                }.each{
                    if(!views.containsKey(it.logicalPropertyName)){
                        views[it.logicalPropertyName] = [
                            views:[
                                standard : new ViewAll()
                            ]
                        ]
                    }
                }
        }

        if (views){
            application.config.domainViews = views
            def domainViewsService = new DomainViewsService(grailsApplication: application)
            println "******************************** Normalizing views ********************************"
            application.config.domainViews.each{domainName,_ ->
                def domainClass = application.getArtefactByLogicalPropertyName("Domain",domainName)
                domainViewsService.normalize domainClass?.clazz
                println "- $domainName [${domainClass?.clazz?.name}]"
                application.config.domainViews[domainName]?.views?.each{viewName,_2 ->
                    println "\t $viewName"
                }
            }
        }
    }

    def doWithApplicationContext = { applicationContext ->
        loadAndNormalize(application)
    }

    def watchedResources = "file:./grails-app/conf/*Views.groovy"

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
        // 
        loadAndNormalize(event.application)
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
        // println "onConfigChange ${event.source}"
        
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
