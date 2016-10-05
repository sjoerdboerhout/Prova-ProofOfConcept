Steps to add OJDBC support:

--------------------------------------------------------------------------------
MAVEN REPO:
https://blogs.oracle.com/dev2dev/entry/how_to_get_oracle_jdbc#settings

--------------------------------------------------------------------------------

OJDBC6:
http://www.oracle.com/technetwork/apps-tech/jdbc-112010-090769.html

mvn install:install-file -Dfile={PathToJar/ojdbc6.jar} -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.4 -Dpackaging=jar


--------------------------------------------------------------------------------
OJDBC7:
http://www.oracle.com/technetwork/database/features/jdbc/jdbc-drivers-12c-download-1958347.html

mvn install:install-file -Dfile={PathToJar/ojdbc7.jar} -DgroupId=com.oracle -DartifactId=ojdbc7 -Dversion=12.1.0.1 -Dpackaging=jar