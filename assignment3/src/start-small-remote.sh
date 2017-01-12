#!/bin/bash

#pkill rmiregistry
#sleep 1
rmiregistry &
sleep 3

LOCAL_IP="145.94.198.10"

IP_1="localhost"
IP_2="145.94.171.197"

function run {
    #java -Djava.security.policy=my.policy Process $1 $2 $3 &
    java -Djava.security.policy=my.policy -Djava.rmi.server.hostname=$LOCAL_IP Process $1 $2 $3 &
}


n1="n1:$IP_1"
n2="n2:$IP_1"
n3="n3:$IP_2"
n4="n4:$IP_2"

n1_n4="$n4:8"
n1_n2="$n2:1"
          
n2_n1="$n1:1"
n2_n3="$n3:5"
          
n3_n2="$n2:5"
n3_n4="$n4:2"
          
n4_n3="$n3:2"
n4_n1="$n1:8"

run "n1" "$n1_n4" "$n1_n2" >> n1.out 2>&1
run "n2" "$n2_n1" "$n2_n3" >> n2.out 2>&1
#run "n3" "$n3_n2" "$n3_n4" >> n3.out 2>&1
#run "n4" "$n4_n3" "$n4_n1" >> n4.out 2>&1

multitail n1.out n2.out
