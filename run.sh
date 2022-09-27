#!/usr/bin/env bash

javac -cp src/:lib/* -d bin/ src/[HWName]/RunAllTests.java
java -cp bin/:lib/* AutoGrader.GradescopeAutoGrader
cp results.json /autograder/results
