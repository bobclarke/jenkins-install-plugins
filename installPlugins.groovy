//----------------------------------------------------------------------
// Setup
//----------------------------------------------------------------------
def jenkinsHost = args[0]
def jenkinsPort = args[1]
def jenkinsUser = args[2]
def jenkinsPass = args[3]
def pluginsRequested = args[4]
def slurper = new groovy.json.JsonSlurper()
def addr = "http://${jenkinsHost}:${jenkinsPort}/pluginManager/api/json?depth=5"
def authString = "${jenkinsUser}:${jenkinsPass}".getBytes().encodeBase64().toString()


//----------------------------------------------------------------------
// Main
//----------------------------------------------------------------------
pluginDir = '/Users/Shared/Jenkins/Home/plugins'
installedPlugins = getInstalledPlugins( slurper, addr, authString )
pluginsToInstall = pluginsRequested.split(/,/)

pluginsToInstall.each{ requestedPlugin ->
	println "Checking if ${requestedPlugin} is installed"
	if ( installedPlugins.shortName.contains( requestedPlugin ) ){
		println "${requestedPlugin} is already installed - skipping"
	} else {
		println "Installing ${requestedPlugin}"
		downloadPlugin( requestedPlugin )
	}
}


//----------------------------------------------------------------------
// Functions
//----------------------------------------------------------------------

private downloadPlugin( requestedPlugin ){

	// Download hpi file
	def fileName = pluginDir +'/'+ requestedPlugin + '.hpi'
	def url = 'http://updates.jenkins-ci.org/latest/' + requestedPlugin + '.hpi'  
        def file = new File( fileName ).newOutputStream()  
        file << new URL(url).openStream()  
        file.close()  
	downloadDependancies( fileName )
}

private downloadDependancies( fileName ){
	println "Checking dependancies for ${fileName}"
	new java.util.jar.JarFile( fileName ).manifest.mainAttributes.entrySet().each {
   		if ( it.key.toString().toLowerCase().contains( "Dependencies".toLowerCase() ) ) {
			def deps = it.value.split(/,/)
			deps.each{ dep ->
				def tmp = dep.split(/:/)
				def depName = tmp[0]
				println "Found dependency: ${dep}"
			        if ( installedPlugins.shortName.contains( depName ) ){
                			println "${depName} dependency is already installed - skipping"
				} else {
					println "Installing dependency ${depName}"
					downloadPlugin( depName )
				}
			}
		}
	}
}

private getInstalledPlugins( slurper, addr, authString ){
	def conn = addr.toURL().openConnection()
	conn.setRequestProperty( "Authorization", "Basic ${authString}" )
	if( conn.responseCode == 200 ) {
		def resp = slurper.parseText( conn.content.text )
		return resp.plugins
	} else {
		println "ERROR"
		println "${conn.responseCode}: ${conn.responseMessage}" 
	}
}
