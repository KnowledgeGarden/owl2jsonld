# owl2jsonld
Read OWL documents; return JSON-LD as gzip file

Some tests in /devtests show how the API can be used.

If the class **Environment** is booted with anything passed in as a parameter, then they system automatically reads OWL documents in /data directory and returns JSON-LD files in the /output directory.

Compiled and tested on JDK 1.8

License: Apache2