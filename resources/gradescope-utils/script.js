const settings = {
    // Hide old instructor courses: Use the 's' key
    // on the homepage to toggle visibility of
    // instructor courses from previous terms
    hideOldInstructorCourses: true,
    // Force enable SSH option: Allows for clicking
    // 'Debug via SSH' when Gradescope thinks a
    // session is still running
    forceEnableSshOption: true,
    // Incomplete grading progress bar: Sets progress
    // bar color to red if not at 100%
    incompleteGradingProgressBar: true,
    // Show autograder scores: Use the '`' (backtick)
    // key to show scores next to methods while
    // grading programming assignments
    showAutograderScores: true,
};

const toggleDisplay = (hidden) => {
    const title = document.querySelector("#account-show > div.courseList:nth-child(2) > .courseList--term:not(:first-child)");
    const courses = document.querySelector("#account-show > div.courseList:nth-child(2) > .courseList--coursesForTerm:nth-child(4)");
    title.style.display = (hidden) ? '' : 'none';
    courses.style.display = (hidden) ? '' : 'none';
    return !hidden;
};

const clickBtn = () => {
    try {
        const showMore = document.querySelectorAll('.tiiBtn.js-viewInactive')[0];
        showMore.click();
    } catch (e) {
        // Ignore
    }
};

if (settings.hideOldInstructorCourses && window.location.pathname == '/') {
    let hidden = false;
    window.addEventListener('keydown', e => {
        if (e.key == 's') {
            hidden = toggleDisplay(hidden);
            clickBtn();
        }
    });
    document.querySelectorAll('.tiiBtn.js-viewInactive')[0].style.display = 'none';
    hidden = toggleDisplay();
}

if (settings.forceEnableSshOption && document.querySelector("#actionBar > ul > li:nth-child(3) > span > span > button")) {
    document.querySelector("#actionBar > ul > li:nth-child(3) > span > span > button").classList.remove('disabled');
}

if (settings.incompleteGradingProgressBar) {
    document.querySelectorAll('td > div > div > div.progressBar--fill').forEach(item => {
        let width = item.getAttribute('style');
        width = width.substring(width.indexOf(' '), width.indexOf('%'));
        if (Number(width) < 100) {
            item.style.backgroundColor = 'var(--tdl-color-semantic-negative)';
        }
    });
}

if (settings.showAutograderScores && window.location.pathname.includes('/grade')) {
    if (document.querySelector('.autograderResultsContainer i').classList.contains('fa-caret-right')) {
        document.querySelector('.autograderResultsContainer i').click();
    }
    window.addEventListener('keydown', e => {
        if (e.key == '`') {
            const titles = [...document.querySelectorAll('.hljs-title')];
            [...document.querySelectorAll('.testCase a')]
                .filter(test => test.innerText.includes('.'))
                .forEach(test =>
                    titles
                        .filter(item => test.innerText.toLowerCase().includes(item.innerText.toLowerCase()))
                        .forEach(item => item.innerText += ' ' + test.innerText.split(' ').at(-1)));
        }
    });
}