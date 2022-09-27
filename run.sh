#!/usr/bin/env bash

# On line 6, change [project] to the project RunAllTests.java is part of
# Add test files to the src directory
# Zip files (not the containing folder) and upload to assignment
javac -cp src/:lib/* -d bin/ src/[project]/RunAllTests.java
java -cp bin/:lib/* AutoGrader.GradescopeAutoGrader
cp results.json /autograder/results
