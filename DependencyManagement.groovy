class DependencyManagement {
    def inlinePlugins = [
            'transmart-core': 'transmart-core-db',
            'rdc-modules': 'Rmodules',
            'folder-management': 'folder-management-plugin'
    ]

    def configureRepositories(dsl) {
        dsl.repositories {
            grailsCentral()
            mavenCentral()

            mavenRepo "https://repo.transmartfoundation.org/content/repositories/public/"
            mavenRepo "https://repo.thehyve.nl/content/repositories/public/"
        }
    }

    class InternalDependenciesFilter {
        def dsl

        def invokeMethod(String name, args) {
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
            compile ':rdc-rmodules:1.2.4'
            runtime ':transmart-core:1.2.4'
            compile ':transmart-gwas:1.2.4'
            //// already included in transmart-gwas
            compile ':transmart-legacy-db:1.2.4'
            //// already included in transmart-gwas
            //compile ':folder-management:1.2.4'
            //// already included in transmart-gwas, folder-management
            compile ':search-domain:1.2.4'
            //// already included in search-domain, transmart-gwas,
            //                       folder-management
            compile ':biomart-domain:1.2.4'
            //// already included in biomart-domain
            compile ':transmart-java:1.2.4'
            runtime ':dalliance-plugin:0.2-SNAPSHOT'
            runtime ':transmart-mydas:0.1-SNAPSHOT'
            runtime(':transmart-rest-api:1.2.4') {
                excludes 'transmart-core'
                excludes 'transmart-core-db-tests'
            }
            runtime ':blend4j-plugin:1.2.4'
            runtime ':transmart-metacore-plugin:1.2.4'

            test ':transmart-core-db-tests:1.2.4'
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
