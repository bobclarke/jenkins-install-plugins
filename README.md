# jenkins-install-plugins

Operation:
- User provides a list of plugins to install (short names, comma separated), this is fed into an array 
- Script queries PluginManager via REST, builds array of installed plugins
- Two arrays are compared
- Plugins that are not already installed are downloaded into Jenkins plugin directory
- The manifest of the downloaded plugin HPI is checked to determine dependancies (NOTE: I would have preferred to do this via REST however a restart of Jenkins would have been required to install each plugin so that dependancies could be checked in this way.
- Each dependency is fed back to the downloadPlugin method and treated like the original plugin, thus any recursive dependencies are resolved. The getDependencies and downloadPlugin methods continue to have this neat little back and forth comms until all plugins are downloaded.
- I would have preferred to actually install the plugin via the REST interface thus providing the option not to run this script locally on the Jenkins server however time was not in my favour. Maybe in version 2 :-)



Usage: groovy installPlugins.groovy <jenkins host> <jenkins port> <username> <password> "comma separated list of plugin short names"'
Exmaple: groovy installPlugins.groovy localhost 8080 mylogin mypass "cucumber-trends-report,terraform,emma"
  
Assumes: 
groovy 2.* is installed 
script is run locally on the Jenkins server in question. 
	
