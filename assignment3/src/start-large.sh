#!/bin/bash

rmiregistry &
sleep 3

function run {
    java -Djava.security.policy=my.policy Process $1 &
}


n1="n1:localhost"
n2="n2:localhost"
n3="n3:localhost"
n4="n4:localhost"
n5="n5:localhost"
n6="n6:localhost"

n1_n2="$n2:7"
n1_n5="$n5:6"
n1_n6="$n6:1"
          
n2_n1="$n1:7"
n2_n3="$n3:2"
n2_n6="$n6:8"
          
n3_n2="$n2:2"
n3_n4="$n4:9"
n3_n6="$n6:4"
          
n4_n3="$n3:9"
n4_n5="$n5:3"
n4_n6="$n6:10"
          
n5_n1="$n1:6"
n5_n4="$n4:3"
n5_n6="$n6:5"
          
n6_n1="$n1:1"
n6_n2="$n2:8"
n6_n3="$n3:4"
n6_n4="$n4:10"
n6_n5="$n5:5"
          

run "n1 $n1_n5 $n1_n2 $n1_n6" >> n1.out 2>&1
run "n2 $n2_n1 $n2_n3 $n2_n6" >> n2.out 2>&1
run "n3 $n3_n2 $n3_n4 $n3_n6" >> n3.out 2>&1
run "n4 $n4_n3 $n4_n5 $n4_n6" >> n4.out 2>&1
run "n5 $n5_n4 $n5_n1 $n5_n6" >> n5.out 2>&1
run "n6 $n6_n1 $n6_n2 $n6_n3 $n6_n4 $n6_n5" >> n6.out 2>&1

multitail n1.out n2.out n3.out n4.out n5.out n6.out
