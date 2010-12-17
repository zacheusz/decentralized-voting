#!/bin/bash

source ../../../configure.sh

nb_nodes=`ls *.out | wc -l`
true_rcv=`cat *.out | grep "Received" | grep "ballot" | grep "true" | wc -l`
true_sent=`cat *.out | grep "Send" | grep "ballot" | grep "true" | wc -l`
false_rcv=`cat *.out | grep "Received" | grep "ballot" | grep "false" | wc -l`
false_sent=`cat *.out | grep "Send" | grep "ballot" | grep "false" | wc -l`
nb_voters=$((($true_sent+$false_sent)/$NB_BALLOTS))

echo "General results"

echo " - Participants: $nb_nodes"
echo " - Voters: $nb_voters"
echo " - Loss rate: <$((100-(100*$nb_voters/$nb_nodes)))%"
# echo " - Actual tally: $(($true_sent-$false_sent))"

echo "Exact results"
echo " - Sent ballots: $(($true_sent+$false_sent)) ($true_sent 'true', $false_sent 'false')"
echo " - Received ballots: $(($true_rcv+$false_rcv)) ($true_rcv 'true', $false_rcv 'false')"
echo " - Loss rate: <$((100-100*($true_rcv+$false_rcv)/($true_sent+$false_sent)))%"

echo "Detailled results"

REAL_TALLY=""
REAL_TALLY_N=0

for ((i=0 ; i<$NB_GROUPS ; i=$i+1)) do

j=$((($i+1)%$NB_GROUPS))

echo " - Group $i"

trueb=`cat *.out | grep "($i): Send" | grep "ballot" | grep "true" | wc -l`
falseb=`cat *.out | grep "($i): Send" | grep "ballot" | grep "false" | wc -l`
echo "   - Group $i: sent tally $(($trueb-$falseb))"
REAL_TALLY="$REAL_TALLY $(($trueb-$falseb))"
REAL_TALLY_N=$(($REAL_TALLY_N + $trueb-$falseb))

rcv_tally=`cat *.out | grep "($j):" | grep "tally=" | cut -d'=' -f2 | mysum | cut -d'.' -f1`
echo "   - Group $j received tally $rcv_tally"

cnt_tally=`cat *.out | grep "($j): local tally" | cut -d':' -f4 | tr '\n' ' '`
echo "   - Group $j counted $cnt_tally"

echo "----------------------------------"

done

echo "Misc"
echo " - Real tally $REAL_TALLY ($REAL_TALLY_N)"
tcp_error=`cat *.out | grep -c "TCP"`
udp_error=`cat *.out | grep -c "UDP"`
echo " - Network errors: UDP $udp_error, TCP $tcp_error"

peer_error=`grep "reason" *.out | grep -c "no peer"`
echo " - Warning: No peer $peer_error, High load: $highload_error "

nb_stop=`grep -L "self" *.out | wc -l`
iam_error=`grep "reason" *.out | grep "IAM" | grep -c "too late"`
proxy_error=`grep "no proxy" *.out | wc -l`
highload_error=`grep "CPU" *.out | wc -l`
god_error=`grep "reason" *.out | grep -c "God"`

echo " - Abnormal termination ($nb_stop): Late registration $iam_error, No proxy $proxy_error God: $god_error"

alpha=`grep "Param" *.out | head -1 | cut -d'=' -f2`
mean=`grep "Final" *.out | grep -v "_" | cut -d':' -f4 | cut -d'(' -f2 | cut -d')' -f1 | myaverage`
std=`grep "Final" *.out | grep -v "_" | cut -d':' -f4 | cut -d'(' -f2 | cut -d')' -f1 | mystd`
outputs=`grep "Final" *.out | wc -l`
ignore=`grep "Final" *.out | grep "_" | wc -l`
rstd=`grep "Final" *.out | grep -v "_" | cut -d':' -f4 | cut -d'(' -f2 | cut -d')' -f1 | mystd $REAL_TALLY_N`
propplus=`grep "Final" *.out | grep -v "_" | cut -d':' -f4 | cut -d'(' -f2 | cut -d')' -f1 | grep -c -v "-"`
propmoins=`grep "Final" *.out | grep -v "_" | cut -d':' -f4 | cut -d'(' -f2 | cut -d')' -f1 | grep -c "-"`
alphahat=$(($((500*$REAL_TALLY_N/$nb_voters))+500))

if [[ $REAL_TALLY_N -gt 0 ]]
then propright=`echo "scale=2;100*$propplus/($propplus+$propmoins)" | bc`
fi

if [[ $REAL_TALLY_N -lt 0 ]]
then propright=`echo "scale=2;100*$propmoins/($propplus+$propmoins)" | bc`
fi

ignore=`echo "scale=2;100*$ignore/$outputs" | bc`

echo "GnuPlot results (alpha,tally,avg est tally,std dev, _:"
echo "$alpha $REAL_TALLY_N $mean $std $rstd $ignore $propright $alphahat"

# for i in 0809112*; do cd $i; ../../../getStats.sh > stats; tail -1 stats; cd ..; done
# for i in 0809112*; do cd $i; tail -1 stats; cd ..; done | cut -d' ' -f5 | myaverage
