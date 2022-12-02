#!/usr/bin/env bash

TESTS="" # List of test packages, separated by spaces
SCORE=0 # Maximum total score

shopt -s globstar
javac -cp src/:lib/* -d bin/ src/**/*.java &> java.out
OUT=`cat java.out`
if [ -z "$OUT" ]
then
java -cp bin/:lib/* AutoGrader.GradescopeAutoGrader $SCORE $TESTS
cp results.json /autograder/results
else
echo "There was a problem compiling the student's submission"
echo java.out
fi
