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

## Prerequisites
- Assignment JUnit tests can be graded without intervention
- Students are not submitting graphics work and are not creating their own JUnit tests
- The first line of every Java file declares a package
- `RunAllTests.java` runs all tests (doesn't work with multiple test sessions)
  - To fix this issue, combine all tests into one file. Submit this to the Autograder but give students the original test files.

## Getting started
1. Download this repository
2. Open `run.sh` and update [HWName] to the folder `RunAllTests.java` is located in (usually the name of the assignment).
3. Open `src/AutoGrader/AutoGrader.java` update [HWName] to the same value as above. Example: if the homework name is HW1, then [HWName] = HW1.
4. Import Java test files to the `src/` directory. Do not upload starter code or anything else a student should modify.
5. Open `src/[HWName]/RunAllTests.java` and add `import AutoGrader.*;` after the package line. Add an instance of AutoGrader in the RunAllTests class by including:
```
static public GradescopeAutoGrader g = new GradescopeAutoGrader([numberOfTests], [assignmentTotalScore]);
```
Replace [numberOfTests] with the sum of tests executed, and replace [assignmentTotalScore] with the highest number of points a student can receive on the assignment.

6. In the outputResults method, add the following code:
```
g.addTest(testClassName, numberOfTests, [visibility]);
g.addResult(testClassName, testsPassed);
```
[visibility] is optional (defaults to after_due_date) and can be set to `hidden`, `after_due_date`, `after_published`, or `visible `. Automatically updates to visible if a subset of tests executed is zero.

7. In `RunAllTestsTearDown.java`, add `RunAllTests.g.toJSON();` to the end of the percentagePassed method.
8. Zip the contents (everything inside the downloaded folder) and upload to the Gradescope assignment.

## Questions
Example ZIP files can be accessed [here](https://rosehulman-my.sharepoint.com/:f:/g/personal/marandcp_rose-hulman_edu/EjTr2MwEyc9Mrvg-3bEq6PYBLGGNiO-5toKg1S3GnjlXPw?e=6TiqYu)
If you need help using this program, contact me [(Canon Maranda)](https://link.canon.click/from/github).
