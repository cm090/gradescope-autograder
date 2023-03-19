#!/usr/bin/env bash

TESTS="" # List of test packages, separated by spaces
SCORE=0 # Maximum total score

shopt -s globstar
ecj -cp lib/hamcrest-core-1.3.jar:lib/junit-4.13.2.jar -d bin/ -Xlint -nowarn -1.9 -proceedOnError src/ &> java.out
java -cp bin/:lib/* AutoGrader.GradescopeAutoGrader $SCORE $TESTS &> java.stdout
cp results.json /autograder/results
cat results.out
