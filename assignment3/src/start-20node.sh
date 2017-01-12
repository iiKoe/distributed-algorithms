#!/bin/bash

rmiregistry &
sleep 1

function run {
    java -Djava.security.policy=my.policy Process $1 $2 > $1.out 2>&1 &
}

function l {
    echo "$1:localhost"
}

run "n1"  "$(l "n2" ):2    $(l "n4" ):3 "
run "n2"  "$(l "n1" ):2    $(l "n3" ):7   $(l "n5" ):1"
run "n3"  "$(l "n2" ):7    $(l "n5" ):6   $(l "n7" ):15"
run "n4"  "$(l "n1" ):3    $(l "n6" ):16  $(l "n9" ):17"
run "n5"  "$(l "n2" ):1    $(l "n3" ):6   $(l "n6" ):11 $(l "n7" ):5  $(l "n8" ):10"
run "n6"  "$(l "n4" ):16   $(l "n5" ):11  $(l "n8" ):4  $(l "n9" ):8"
run "n7"  "$(l "n3" ):15   $(l "n5" ):5   $(l "n8" ):12 $(l "n10"):13"
run "n8"  "$(l "n5" ):10   $(l "n6" ):4   $(l "n7" ):12 $(l "n9" ):18 $(l "n10"):9"
run "n9"  "$(l "n4" ):17   $(l "n6" ):8   $(l "n8" ):18 $(l "n10"):14 $(l "n12"):60"
run "n10" "$(l "n7" ):13   $(l "n8" ):9   $(l "n9" ):14 $(l "n11" ):50"

run "n11"  "$(l "n12" ):22    $(l "n14" ):23   $(l "n10" ):50"
run "n12"  "$(l "n11" ):22    $(l "n13" ):27   $(l "n15" ):21 $(l "n9"):60"
run "n13"  "$(l "n12" ):27    $(l "n15" ):26   $(l "n17" ):35"
run "n14"  "$(l "n11" ):23    $(l "n16" ):36   $(l "n19" ):37"
run "n15"  "$(l "n12" ):21    $(l "n13" ):26   $(l "n16" ):31  $(l "n17" ):25  $(l "n18" ):30"
run "n16"  "$(l "n14" ):36    $(l "n15" ):31   $(l "n18" ):24  $(l "n19" ):28"
run "n17"  "$(l "n13" ):35    $(l "n15" ):25   $(l "n18" ):32  $(l "n20" ):33"
run "n18"  "$(l "n15" ):30    $(l "n16" ):24   $(l "n17" ):32  $(l "n19" ):38  $(l "n20"):29"
run "n19"  "$(l "n14" ):37    $(l "n16" ):28   $(l "n18" ):38  $(l "n20" ):34"
run "n20"  "$(l "n17" ):33    $(l "n18" ):29   $(l "n19" ):34"

#multitail n1.out n2.out n3.out n4.out n5.out n6.out n7.out n8.out
multitail n1.out n2.out n3.out n4.out n5.out n6.out n7.out n8.out n9.out n10.out
