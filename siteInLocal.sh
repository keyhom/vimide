#!/bin/bash

# set the script working directory.
cd `dirname $0`

# run the jetty-maven-plugin to startup web.
mvn jetty:run -Dsite.local=true -N

exit
