#! /bin/bash

if [ $# = 1 ]
then
    echo 'cc-keybrl-node0:'
    ssh keybrl@cc-keybrl-node0.local $1
    echo 'cc-keybrl-node1:'
    ssh keybrl@cc-keybrl-node1.local $1
    echo 'cc-keybrl-node2:'
    ssh keybrl@cc-keybrl-node2.local $1
elif [ $# = 2 ]
then
    echo 'cc-keybrl-node0:'
    ssh $1@cc-keybrl-node0.local $2
    echo 'cc-keybrl-node1:'
    ssh $1@cc-keybrl-node1.local $2
    echo 'cc-keybrl-node2:'
    ssh $1@cc-keybrl-node2.local $2
fi

