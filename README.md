
[jetty]: http://eclipse.org/jetty/
[maven]: http://maven.apache.org/
[java]: https://www.oracle.com/java/index.html
[git]: https://git-scm.com/
[make]: https://www.gnu.org/software/make
[docker]: https://www.docker.com/

Redirect Service
===================================

Redirects web requests to URLs set via table file

This service runs using an embedded [Jetty][jetty] server and is invoked
from the command line. 


Requirements
=============

* MacOS, Rocky Linux 8+, Ubuntu 20+, and most other Linux distributions should work
* [Java][java] 17+ **(jdk to build)**
* [Make][make] **(to build)**
* [Maven][maven] 3.6 or higher **(to build)**

Building Redirect Service
=========================================

Commands build Redirect Service assuming machine has [Git][git] command line tools 
installed and above Java modules have been installed.

```Bash
# In lieu of git one can just download repo and unzip it
git clone https://github.com/ndexbio/redirect-service.git

cd redirect-service
mvn clean install
```

The above command will create a jar file under **target/** named  
**redirect-service-\<VERSION\>-jar-with-dependencies.jar** that
is a command line application

Running Redirect Service locally
==================================================

```Bash
# In lieu of git one can just download repo and unzip it
git clone https://github.com/ndexbio/redirect-service.git

cd redirect-service
mvn clean install
mkdir -p /tmp/redirect/logs
cp systemd/server.conf /tmp/redirect/.
cp systemd/redirects.csv /tmp/redirect/.
java -jar target/redirect-service*jar --conf /tmp/redirect/server.conf --mode runserver
```
