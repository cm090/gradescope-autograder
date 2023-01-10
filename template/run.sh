#!/usr/bin/env bash

TESTS="" # List of test packages, separated by spaces
SCORE=0 # Maximum total score

shopt -s globstar
javac -cp src/:lib/* -d bin/ src/**/*.java -Xlint &> java.out
OUT=`cat java.out`
if [ -z "$OUT" ]
then
java -cp bin/:lib/* AutoGrader.GradescopeAutoGrader $SCORE $TESTS
cp results.json /autograder/results
cat results.out
else
echo "There was a problem compiling the submission. Please check the following output:"
cat java.out
fi
