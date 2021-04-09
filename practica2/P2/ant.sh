#!/bin/bash
for i in P1-base P1-ejb-servidor-remoto P1-ejb-cliente-remoto; do
cd $i
ant unsetup-db
ant replegar; ant delete-pool-local
cd -
done
cd P1-ws
ant unsetup-db
ant replegar-servicio; ant replegar-cliente; ant delete-pool-local
cd -
cd P1-base
ant delete-db
cd -
for i in P1-base P1-ejb-servidor-remoto P1-ejb-cliente-remoto P1-ws; do
cd $i
ant limpiar-todo 
cd -
done