
The source code is an Eclipse 4.4.2 Java project. 

It can be installed by placing the JDDB folder inside of the Eclipse workspace 
and use the project import wizard. Once the project has been imported, there 
is an autobuild script that should build JAR files for each of the 3 project 
components and place them in the /build/ directory. 

To run the project, first start up the manager node by running:

> java -jar jddb-manager config.properties

Then start the client node in a different console:

> java -jar jddb-master config.properties

Finally start each shard node to host the data:

> java -jar jddb-shard config.properties

