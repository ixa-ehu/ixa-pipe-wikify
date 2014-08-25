#!/usr/bin/env bash

set -e

##
# Enters the directory given in the first argument and notifies the user about
# this.
#
# @param [String] $1 The directory to change the current working directory to.
#
enter_directory()
{
    full_path=$(readlink -f $1)

    echo "Changing PWD to ${full_path}"
    cd $1
}

##
# Shows the message passed as the first argument and terminates the script.
#
# @param [String] $1 The message to display.
#
abort()
{
    echo $1
    exit 1
}

spotlight="${1}"

if [[ -z "${spotlight}" ]]
then
    abort "You must specify the spotlight.jar as the first argument"
fi

echo 'Installing dbpedia-spotlight JAR as a local maven repository...'

mvn install:install-file \
    -Dfile=$spotlight \
    -DgroupId=ixa -DartifactId=dbpedia-spotlight -Dversion=0.7 \
    -Dpackaging=jar -DgeneratePom=true

enter_directory "${old_pwd}"

echo 'Finished installing'
