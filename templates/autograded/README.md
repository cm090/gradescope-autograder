# Gradescope Autograder

### Template for Rose-Hulman CSSE220

## File structure

- config.json
  - Provides the autograder with the following information:
    - `classes`: List of test packages, score weights (must be >= -1), and number of test classes to drop
    - `additional_options.type`: Assignment type (exam, homework)
    - `additional_options.test_visibility`: Whether to show the test results to students (hidden, after_due_date, after_published, visible)
    - `additional_options.timeout_seconds`: How long to run a test before timing out
    - `additional_options.starter_code_download`: Link for students to download starter code (exams only)
    - `additional_options.extra_credit_tests`: Number of extra credit tests (works best when all package weights are -1)
- download_starter_code_template
  - Displays instructions and a link to download the starter code
- results_error_template
  - Displays a message to the student if the autograder fails to run
- run_autograder
  - Parses student uploads
- run.sh
  - Starts the JUnit tests, moves `results.json` to the correct directory
- setup.sh
  - Downloads required dependencies on setup
- lib/
  - Holds `.jar` files to process code
- src/
  - Location of all uploaded Java files
  - autograder/
    - Runs all tests and processes results
    - calc/
      - Autograder score calculation types

## Prerequisites

- Assignment JUnit tests can be graded without intervention
- Students are not submitting graphics work and are not creating their own JUnit tests
- The first line of every Java file declares a package

## Getting started

1. Download this repository
2. Use the [builder tool](https://github.com/cm090/gradescope-autograder/releases/download/latest/AutograderBuilder.jar) to automatically generate the autograder zip file. Use the advanced process below if you need to make additional changes.
3. Open `config.json` and update line the contents.
4. Import Java test files to the `src/` directory. Do not upload starter code or anything else a student should modify.
5. Zip the contents (everything inside the downloaded folder) and upload to the Gradescope assignment.

## Questions

Check out the [wiki](https://github.com/cm090/gradescope-autograder/wiki)

If you need help using this program, [send a message](https://link.canon.click/from/github).
