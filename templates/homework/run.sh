#!/usr/bin/env bash

# Constants
CONFIG_FILE="config.json"
IMPORT_ERROR="# ERROR: Grading Failed
**We were unable to locate one or more files.** Please make sure of the following:
1. You have uploaded all required *.java* files
2. The first line of each file begins with *\"package\"*

If you're still having trouble, please contact an instructor or TA."

shopt -s globstar &> /dev/null
# Allows us to compile code while ignoring errors (very important)
ecj -cp lib/hamcrest-core-1.3.jar:lib/junit-4.13.2.jar:lib/json-20231013.jar -d bin/ -Xlint -nowarn -1.9 -proceedOnError src/ &> java.out
java -cp bin/:lib/* AutoGrader.GradescopeAutoGrader $CONFIG_FILE &> java.stdout
json_pp < results.json > /autograder/results/results.json

# TestRunner.java will throw results to this file. Cleans the output for Gradescope
cat results.out

# If a file is missing, alert the student (false positives are possible)
if grep -q ".*The import [^ ]* cannot be resolved.*" java.out
then
    echo "One or more files are missing. See compiler output for details."
    jq --arg msg "$IMPORT_ERROR" '.output = $msg' /autograder/results/results.json > temp && mv temp /autograder/results/results.json
fi

# Upload compiler and runtime outputs, if files aren't empty
OUTPUT_TEXT=""
if [ -s java.out ]
then
    OUTPUT_TEXT=+"<details><summary><b>Compiler output</b></summary><pre>$(cat java.out)</pre></details>"
fi
if [ -s java.stdout ]
then
    OUTPUT_TEXT+="<details><summary><b>Runtime output</b></summary><pre>$(cat java.stdout)</pre></details>"
fi

# Add links to the results.json file, if they exist
if [ -n "$OUTPUT_TEXT" ]
then
    OUTPUT_TEXT=$(echo "$OUTPUT_TEXT" | sed -z 's/\n/\\n/g')
    OUTPUT_JSON="{
          \"status\": \"passed\",
          \"name\": \"Console Output\",
          \"output\": \"$OUTPUT_TEXT\",
          \"output_format\": \"html\",
          \"visibility\": \"hidden\"
        }"
    jq --argjson new_test "$OUTPUT_JSON" '.tests += [$new_test]' /autograder/results/results.json > temp && mv temp /autograder/results/results.json
fi
