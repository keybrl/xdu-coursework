#！/bin/bash

if [ $# = 1 ]
then
    scp $1 keybrl@cc-keybrl-node1.local:$1
    scp $1 keybrl@cc-keybrl-node2.local:$1
elif [ $# = 2 ]
then
    scp $1 keybrl@cc-keybrl-node1.local:$2
    scp $1 keybrl@cc-keybrl-node2.local:$2
elif [ $# = 3 ]
then
    scp $1 $3@cc-keybrl-node1.local:$2
    scp $1 $3@cc-keybrl-node2.local:$2
elif [ $# = 4 ]
then
    scp $4 $1 $3@cc-keybrl-node1.local:$2
    scp $4 $1 $3@cc-keybrl-node2.local:$2
else
    scp /usr/local/hadoop/etc/hadoop/* keybrl@cc-keybrl-node1.local:/usr/local/hadoop/etc/hadoop/
    scp /usr/local/hadoop/etc/hadoop/* keybrl@cc-keybrl-node2.local:/usr/local/hadoop/etc/hadoop/
fi
 
