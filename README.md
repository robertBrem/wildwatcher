# wildwatcher
Monitoring for Wildfly

You can test it with the following possibilites:
[http://localhost:8080/WildWatcher/resources/servers/10.10.10.10](http://localhost:8080/WildWatcher/resources/servers/10.10.10.10)
results in -->
```
[
	{"server-state":"\"running\""},
	{"launch-type":"\"STANDALONE\""},
	{"management-major-version":"2"},
	{"management-micro-version":"0"},
	{"management-minor-version":"2"},
	{"name":"\"4c96f212980a\""},
	{"namespaces":"[]"},
	{"process-type":"\"Server\""},
	{"product-name":"null"},
	{"product-version":"null"},
	{"profile-name":"null"},
	{"release-codename":"\"Tweek\""},
	{"release-version":"\"8.2.0.Final\""},
	{"running-mode":"\"NORMAL\""},
	{"schema-locations":"[]"}
]
```

[http://localhost:8080/WildWatcher/resources/servers/10.10.10.10:9990/deployments/service.war](http://localhost:8080/WildWatcher/resources/servers/10.10.10.10:9990/deployments/service.war)
results in -->
```
[
	{"content":
		"[
			{ \"path\" : \"deployments/brandservice.war\",
			  \"relative-to\" : \"jboss.server.base.dir\",
			  \"archive\" : true\n}
		]" 
	},
	{"enabled":"true"},
	{"name":"\"brandservice.war\""},
	{"persistent":"false"},
	{"runtime-name":"\"brandservice.war\""},
	{"status":"\"OK\""}
]
```

The port is optional.

You also can set the following QueryParameters:
- username
- password
- realm
[http://localhost:8080/WildWatcher/resources/servers/10.10.10.10:9990/deployments/service.war?username=admin&password=admin&realm=ManagementRealm](http://localhost:8080/WildWatcher/resources/servers/10.10.10.10:9990/deployments/service.war?username=admin&password=admin&realm=ManagementRealm)

[Documentation for the attributes](http://wildscribe.github.io/Wildfly/8.0.0.Final)


