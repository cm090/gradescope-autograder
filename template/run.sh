#!/usr/bin/env bash

TESTS="" # List of test packages, separated by spaces

# NOTE: Do not modify anything above this line if you're using the Autograder builder

shopt -s globstar
# Allows us to compile code while ignoring errors (very important)
ecj -cp lib/hamcrest-core-1.3.jar:lib/junit-4.13.2.jar -d bin/ -Xlint -nowarn -1.9 -proceedOnError src/ &> java.out
java -cp bin/:lib/* AutoGrader.GradescopeAutoGrader $TESTS &> java.stdout
cp results.json /autograder/results
# TestRunner.java will throw results to this file. Cleans the output for Gradescope
cat results.out
