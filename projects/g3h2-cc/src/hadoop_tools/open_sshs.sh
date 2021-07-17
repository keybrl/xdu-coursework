#!/bin/bash

if [ $# = 0 ]
then
    echo 'hhh'
    gnome-terminal -t "title-name" -- bash -c "ssh keybrl@cc-keybrl-node0.local;exec bash;"
    gnome-terminal -t "title-name" -- bash -c "ssh keybrl@cc-keybrl-node1.local;exec bash;"
    gnome-terminal -t "title-name" -- bash -c "ssh keybrl@cc-keybrl-node2.local;exec bash;"
else
    for node in $@
    do
        if [ $node != $1 ]
        then
            gnome-terminal -t "title-name" -- bash -c "ssh '$1'@'$node';exec bash;"
        fi
    done
fi

