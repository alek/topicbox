topicbox | simple topic modeling for fun & profit
--------------------------------

(work in progress - nothing useful here yet)

What's All This [topicbox] Stuff Anyhow ?

* simple (streaming) topic modeling service
* basic UI enabling iteration/debugging of lda models
* enable easy integration with wide range of data sources
* support streaming processing / topic migration callbacks etc.

### Implementation

* Core LDA functionality is provided by Mallet package (http://mallet.cs.umass.edu/)

### License:

Apache Public License (APL) 2.0

### Getting Started

Build :

    mvn clean package

Start the service :

    sh bin/topicbox-server.sh -port 1981

Visit the app :

    http://localhost:1981/webapp/

TBD
