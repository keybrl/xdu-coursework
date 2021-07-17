#! /bin/bash

ssh keybrl@cc-keybrl-node0.local "rm -rf /tmp/hbase-keybrl/zookeeper/*"
ssh keybrl@cc-keybrl-node1.local "rm -rf /tmp/hbase-keybrl/zookeeper/*"
ssh keybrl@cc-keybrl-node2.local "rm -rf /tmp/hbase-keybrl/zookeeper/*"

ssh keybrl@cc-keybrl-node0.local "rm -rf cloud_computing/data/*/*"
ssh keybrl@cc-keybrl-node1.local "rm -rf cloud_computing/data/*/*"
ssh keybrl@cc-keybrl-node2.local "rm -rf cloud_computing/data/*/*"

#ssh keybrl@192.168.31.20 "rm -rf /tmp/hbase-keybrl/zookeeper/*"
#ssh keybrl@192.168.31.20 "rm -rf cloud_computing/data/*/*"
