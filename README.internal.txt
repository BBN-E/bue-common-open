After releasing to Maven Central, checkout the version at the release tag and do an internal deployment as well:

mvn deploy -DaltDeploymentRepository=nexus::default::http://e-nexus-01.bbn.com:8081/nexus/content/repositories/releases/


