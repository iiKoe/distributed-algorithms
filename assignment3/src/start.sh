#!/bin/bash



function run {
    java -Djava.security.policy=my.policy Process $1 $2 $3 &
}


n1="n1:localhost"
n2="n2:localhost"
n3="n3:localhost"
n4="n4:localhost"
n5="n5:localhost"
n6="n6:localhost"
n7="n7:localhost"
n8="n8:localhost"

n1_n8="$n8:8"
n1_n2="$n2:1"
          
n2_n1="$n1:1"
n2_n3="$n3:5"
          
n3_n2="$n2:5"
n3_n4="$n4:3"
          
n4_n3="$n3:3"
n4_n5="$n5:7"
          
n5_n4="$n4:7"
n5_n6="$n6:2"
          
n6_n5="$n5:2"
n6_n7="$n7:6"
          
n7_n6="$n6:6"
n7_n8="$n8:4"
          
n8_n7="$n7:4"
n8_n1="$n1:8"

#echo "starting:" "n1" "$n1_n8" "$n1_n2"
#echo "starting:" "n2" "$n2_n1" "$n2_n3"
#echo "starting:" "n3" "$n3_n2" "$n3_n4"
#echo "starting:" "n4" "$n4_n3" "$n4_n5"
#echo "starting:" "n5" "$n5_n4" "$n5_n6"
#echo "starting:" "n6" "$n6_n5" "$n6_n7"
#echo "starting:" "n7" "$n7_n6" "$n7_n8"
#echo "starting:" "n8" "$n8_n7" "$n8_n1"

run "n1" "$n1_n8" "$n1_n2"
run "n2" "$n2_n1" "$n2_n3"
run "n3" "$n3_n2" "$n3_n4"
run "n4" "$n4_n3" "$n4_n5"
run "n5" "$n5_n4" "$n5_n6"
run "n6" "$n6_n5" "$n6_n7"
run "n7" "$n7_n6" "$n7_n8"
run "n8" "$n8_n7" "$n8_n1"
