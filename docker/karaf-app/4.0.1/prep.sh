#!/bin/sh

if [ -d /prep/etc ]; then
    echo Adding configuration
    mv /prep/etc/* /opt/karaf/etc/. || exit 1
fi
    
if [ -d /prep/deploy ]; then
    echo Adding bundles to deploy
    mv /prep/deploy/* /opt/karaf/deploy/. || exit 1
fi

if [ -f /prep/prep.sh ]; then
    echo Running prep
    chmod +x /prep/prep.sh
    /prep/prep.sh || exit 1
fi
