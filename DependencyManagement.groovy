class DependencyManagement {
    def useSnapshot = false
    def transmartVersion = useSnapshot ? '1.2.2-SNAPSHOT' : '1.2.4'
    def inlinePlugins = useSnapshot ? [:] : [
        'transmart-core': 'transmart-core-db',
        'rdc-rmodules': 'Rmodules',
        'folder-management': 'folder-management-plugin'
    ]

    def configureRepositories(dsl) {
        dsl.repositories {
            grailsCentral()
            mavenCentral()

            mavenRepo "https://tm-dev.sd.genego.com/artifactory/plugins-snapshot-local/"
            mavenRepo "https://tm-dev.sd.genego.com/artifactory/plugins-release-local/"
            mavenRepo "https://repo.transmartfoundation.org/content/repositories/public/"
            mavenRepo "https://repo.thehyve.nl/content/repositories/public/"
        }
    }

    class InternalDependenciesFilter {
        def dsl

        def invokeMethod(String name, args) {
            args[0] = args[0].toString()
            def metaMethod = dsl.metaClass.getMetaMethod(name, args)
            if (inlinePlugins.keySet().any { args[0].contains(":${it}:") }) {
                return null
            }
            def result = metaMethod.invoke(dsl, args)
            return result
        }
    }

    def internalDependencies(dsl) {
        new InternalDependenciesFilter(dsl: dsl).with {
            build ':tomcat:7.0.55.3'

            compile ":rdc-rmodules:$transmartVersion"
            runtime ":transmart-core:$transmartVersion"
            compile ":transmart-gwas:$transmartVersion"
            //// already included in transmart-gwas
            compile ":transmart-legacy-db:$transmartVersion"
            //// already included in transmart-gwas
            //compile ':folder-management:1.2.4'
            //// already included in transmart-gwas, folder-management
            compile ":search-domain:$transmartVersion"
            //// already included in search-domain, transmart-gwas,
            //                       folder-management
            compile ":biomart-domain:$transmartVersion"
            //// already included in biomart-domain
            compile ":transmart-java:$transmartVersion"
            runtime ':dalliance-plugin:0.2-SNAPSHOT'
            runtime ':transmart-mydas:0.1-SNAPSHOT'
            runtime(":transmart-rest-api:$transmartVersion") {
                excludes 'transmart-core'
                excludes 'transmart-core-db-tests'
            }
            runtime ":blend4j-plugin:$transmartVersion"
            runtime ":transmart-metacore-plugin:$transmartVersion"

            //test ':transmart-core-db-tests:1.2.4'
        }
    }

    def configureInternalPlugin(scope, name) {

    }

    def inlineInternalDependencies(grails, grailsSettings) {
//        	grails.plugin.location."summarystatisticsreport" = "/u01/git/summarystatisticsreport.git"
        def dir = new File(getClass().protectionDomain.codeSource.location.path).parentFile
        def projectsDir = dir.parentFile
        inlinePlugins.entrySet().each { entry ->
            print("Plugin ${entry.key} inlined")
            grails.plugin.location[entry.key] = new File(projectsDir, entry.value).canonicalPath
        }
    }
}
