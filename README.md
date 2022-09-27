# Gradescope Autograder
### Template for Rose-Hulman CSSE220

## File structure
- run.sh
  - Starts the JUnit tests, moves `results.json` to the correct directory
  - Requires setting `RunAllTests.java` location
- run_autograder
  - Parses student uploads
- setup.sh
  - Required for upload, no other use
- lib/
  - Holds `.jar` files to process code
- src/
  - Location of all uploaded Java files
  - AutoGrader/
    - AutoGrader.java
      - Stores JUnit results into `results.json`

## Prerequisites
- Assignment JUnit tests can be graded without intervention
- Students are not submitting graphics work and are not creating their own JUnit tests
- The first line of every Java file declares a package

## Getting started
1. Download this repository
2. Open `run.sh` and update [HWName] to the folder `RunAllTests.java` is located in (usually the name of the assignment).
3. Open `src/AutoGrader/AutoGrader.java` update [HWName] to the same value as [project]. Example: if the homework name is HW1, then [HWName] = HW1.
4. Import Java test files to the `src/` directory. Do not upload starter code or anything else a student should modify.
5. Open `src/[HWName]/RunAllTests.java` and add `import AutoGrader.*;` after the package line. Add an instance of AutoGrader in the RunAllTests class by including `static public GradescopeAutoGrader g = new GradescopeAutoGrader();`. In the outputResults method, add the following code:
```
g.addTest(testClassName, numberOfTests);
g.addResult(testClassName, testsPassed, "");
```
The third argument for addResult can take in an output message that is shown to students.
7. Zip the contents (everything inside the downloaded folder) and upload to the Gradescope assignment.

## Questions
If you need help using this program, contact me (Canon Maranda).
