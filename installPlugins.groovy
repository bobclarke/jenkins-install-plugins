//----------------------------------------------------------------------
// installPlugins.groovy
// Author: Bob Clarke
// Date 26/01/2017
//
// Description: Allows user to specify a comma separated list of 
// plug-ins (short name) to be installed
//----------------------------------------------------------------------

//----------------------------------------------------------------------
// Setup
//----------------------------------------------------------------------
if( args.length < 5 ){
	println '\nUsage: groovy installPlugins.groovy <jenkins host> <jenkins port> <username> <password> "comma separated list of plugin short names"'
	println 'Exmaple: groovy installPlugins.groovy localhost 8080 mylogin mypass "cucumber-trends-report,terraform,emma"\n'
	System.exit(0)
}
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

// Get an array of installed plugins
installedPlugins = getInstalledPlugins( slurper, addr, authString )

// Construct an array of plugins tha need to be installed based on user input 
pluginsToInstall = pluginsRequested.split(/,/)

// Compare the arrays
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
	// Need to do a better job at checking HTTP status code here. 
	// Presently, if the url is incorrect the error is not caught
	def fileName = pluginDir +'/'+ requestedPlugin + '.hpi'
	def url = 'http://updates.jenkins-ci.org/latest/' + requestedPlugin + '.hpi'  
        def file = new File( fileName ).newOutputStream()  
        file << new URL(url).openStream()  
        file.close()  

	// Call the getDependencies method
	println "Checking dependancies for ${fileName}"
	getDependancies( fileName )
}

private getDependancies( fileName ){

	// Check the manifest in the hpi file for dependencies
	new java.util.jar.JarFile( fileName ).manifest.mainAttributes.entrySet().each {
   		if ( it.key.toString().toLowerCase().contains( "Dependencies".toLowerCase() ) ) {

			// Split out the dependency name from the version
			def deps = it.value.split(/,/)
			deps.each{ dep ->
				def tmp = dep.split(/:/)
				def depName = tmp[0]
				println "Found dependency: ${dep}"

				// Check if dependency is already installed
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
