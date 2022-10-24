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
- results_template
  - Displays a message to the student if the autograder fails to run
- lib/
  - Holds `.jar` files to process code
- src/
  - Location of all uploaded Java files
  - AutoGrader/
    - AutoGrader.java
      - Stores JUnit results into `results.json`
    - TestRunner.java
      - Runs all unit test files

## Prerequisites
- Assignment JUnit tests can be graded without intervention
- Students are not submitting graphics work and are not creating their own JUnit tests
- The first line of every Java file declares a package

## Getting started
1. Download this repository
2. Open `run.sh` and update lines 3 and 4. This requires listing the packages that contain the unit tests and providing the maximum score.
3. Read the comment in the main method of `src/AutoGrader/AutoGrader.java`.
4. Import Java test files to the `src/` directory. Do not upload starter code or anything else a student should modify.
5. Zip the contents (everything inside the downloaded folder) and upload to the Gradescope assignment.

## Questions
Example ZIP files can be accessed [here](https://rosehulman-my.sharepoint.com/:f:/g/personal/marandcp_rose-hulman_edu/EjTr2MwEyc9Mrvg-3bEq6PYBLGGNiO-5toKg1S3GnjlXPw?e=6TiqYu)

If you need help using this program, contact me [(Canon Maranda)](https://link.canon.click/from/github).
