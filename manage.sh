#!/bin/bash

function unit_tests() {
    mvn clean test
}

function snapshot() {
    read -p "This will reset your current working tree to origin/develop, is this okay? " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]
    then
    git fetch
    git reset --hard origin/develop

    echo "Deploying new release artifacts to sonatype repository."
    mvn clean deploy -P release
    fi
}

function release() {
    read -p "This will reset your current working tree to origin/develop, is this okay? " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]
    then
        git fetch
        git reset --hard origin/develop

        echo "Creating the release branch"
        mvn -Prelease jgitflow:release-start -DpushReleases=true -DautoVersionSubmodules=true

        echo "Merging the release branch into develop & master, pushing changes, and tagging new version off of master"
        mvn -Prelease jgitflow:release-finish -DnoReleaseBuild=true -DpushReleases=true -DnoDeploy=true

        echo "Checking out latest version of master."
        git fetch
        git checkout origin/master

        echo "Deploying new release artifacts to sonatype repository."
        mvn clean deploy -P release
    fi
}

case "$1" in
    snapshot)
        echo -n "Preparing a new snapshot version of the app..."
        release
        echo ""
    ;;
    release)
        echo -n "Preparing to release a new version of the app..."
        release
        echo ""
    ;;
    unit-test)
        echo "Starting unit tests..."
        unit_tests
        echo ""
    ;;
    *)
        echo "Usage: ./manage.sh release|snapshot|unit-test"
        exit 1
esac

exit 0