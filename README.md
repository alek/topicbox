topicbox | simple topic modeling toolkit
----------------------------------------

... just a simple interactive LDA modeling service/ui enabling you to :

* retrieve data from arbitrary sources (filesystem/http/hdfs...) and in arbitrary formats (csv,xml,json..)
* estimate simple LDA model
* browse results (topic/data/co-occurrence matrix views)
* change model configuration params
* repeat
* have fun :)

### Implementation

* Core LDA functionality provided by Mallet package (http://mallet.cs.umass.edu/)

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
