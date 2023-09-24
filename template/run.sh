#!/usr/bin/env bash

TESTS="" # List of test packages, separated by spaces
# NOTE: Do not modify anything above this line if you're using the Autograder builder

IMPORT_ERROR="We were unable to locate one or more files. Please make sure you have uploaded all required .java files and the first line of each file begins with \"package\"."

shopt -s globstar &> /dev/null
# Allows us to compile code while ignoring errors (very important)
ecj -cp lib/hamcrest-core-1.3.jar:lib/junit-4.13.2.jar -d bin/ -Xlint -nowarn -1.9 -proceedOnError src/ &> java.out
java -cp bin/:lib/* AutoGrader.GradescopeAutoGrader $TESTS &> java.stdout
json_pp < results.json > /autograder/results/results.json
# TestRunner.java will throw results to this file. Cleans the output for Gradescope
cat results.out

# If a file is missing, alert the student
if grep -q ".*The import [^ ]* cannot be resolved.*" java.out
then
    echo "One or more files are missing. See java.out for details."
    jq --arg msg "$IMPORT_ERROR" '.output = $msg' /autograder/results/results.json > temp && mv temp /autograder/results/results.json
fi