#!/usr/bin/env bash

for iter in 250 500 750 1000 1250 1500 1750 2000 2250 2500 2750 3000
do
    echo '########################### Iteracion ' $iter ' ###########################'

    # Configure new test
    sed -i "s#<stringProp name=\"ThreadGroup.num_threads\">.*</stringProp>#<stringProp name=\"ThreadGroup.num_threads\">$iter</stringProp>#" P2-curvaProductividad.jmx

    # Launch vmstat and si2-monitor after waiting time
    sshpass -p 2021sid0s ssh si2@10.1.7.2 './run-monitor.sh' &
    ./si2-monitor.sh 10.1.7.2 > monitor.txt &

    # Run tests
    ~/apache-jmeter-5.2.1/bin/jmeter -n -t P2-curvaProductividad.jmx -l aggregate-report.jtl

    # Kill stuff
    sshpass -p 2021sid0s ssh si2@10.1.7.2 'pkill vmstat'
    pkill si2-monitor.sh

    # Collect data
    sshpass -p 2021sid0s scp si2@10.1.7.2:~/cpu-vm2.txt cpu-vm2.txt
    sshpass -p 2021sid0s ssh si2@10.1.7.2 'rm ~/cpu-vm2.txt'
    sshpass -p 2021sid0s ssh si2@10.1.7.1 "psql visa -U alumnodb -c 'DELETE FROM PAGO'"
    
    mv cpu-vm2.txt monitor.txt aggregate-report.jtl ej8/$iter\u
done