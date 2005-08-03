#! /bin/sh
java -cp ./classes:lib/bcel.jar org.cq2.delegator.profiling.Report > report.txt
cat report.txt