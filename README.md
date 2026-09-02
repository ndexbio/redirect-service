
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
cp systemd/pathway_redirects.csv /tmp/redirect/.
java -jar target/redirect-service*jar --conf /tmp/redirect/server.conf --mode runserver

# Visit the following to see the mapping uniprot needs
# http://localhost:8081/uniprot_mapping_file
#
# To test a redirect visit
# http://localhost:8081/ndex
```

Configuration
==================================================

The service reads Java properties from the file passed with `--conf`.

```properties
runserver.log.dir = /tmp/redirect/logs
runserver.port = 8081
redirects.file = /tmp/redirect/redirects.csv
pathway.redirects.file = /tmp/redirect/pathway_redirects.csv
runserver.log.level = INFO
root.log.level = INFO
```

`redirects.file` sets the CSV file used for regular redirects. If this
property is omitted, the service defaults to `redirects.csv` in the current
working directory. The CSV must include this header:

```csv
UNIPROT,ID,OPTIONAL_1,URL
```

Requests to `/{ID}` are matched against the `ID` column and redirect to the
corresponding `URL`. ID matching is case-insensitive.

`pathway.redirects.file` sets the CSV file used for pathway redirects. If this
property is omitted, the service defaults to `pathway_redirects.csv` in the
current working directory. The CSV must include this header:

```csv
ID,URL
```

Requests to `/pathway/{ID}` and `/pathways/{ID}` are matched against the `ID`
column and redirect to the corresponding `URL`. ID matching is case-insensitive.
Set `pathway.redirects.file` to a non-empty CSV file before using these pathway
redirect endpoints.

Service Endpoints
==================================================

* `/{ID}` redirects using `redirects.file`
* `/pathway/{ID}` redirects using `pathway.redirects.file`
* `/pathways/{ID}` redirects using `pathway.redirects.file`
* `/status` returns `OK`
* `/UNIPROT_MAPPING_FILE` returns the UniProt-to-ID mapping generated from
  `redirects.file`

Release Process
==================================================

Releases are created by pushing a version tag that starts with `v`.

Before tagging a release, update `pom.xml` so the project version matches the
release version and does not include `-SNAPSHOT`.

```Bash
mvn test
git tag v1.1.0
git push origin v1.1.0
```

Pushing the tag triggers the GitHub Actions release workflow. The workflow
fails if the tag version does not match the `pom.xml` project version. For
example, tag `v1.1.0` must match `pom.xml` version `1.1.0`.

If the version check passes, the workflow builds the project with:

```Bash
mvn -B clean package
```

It then creates a GitHub Release for the tag and uploads the runnable jar:

```text
target/redirect-service-<VERSION>-jar-with-dependencies.jar
```

Release tags matching `v*` should be protected with a GitHub tag ruleset so
only authorized maintainers can create, update, or delete release tags.
