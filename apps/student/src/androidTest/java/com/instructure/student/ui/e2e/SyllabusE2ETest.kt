/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.student.ui.e2e

import android.util.Log
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class SyllabusE2ETest: StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SYLLABUS, TestCategory.E2E)
    fun testSyllabusE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1, syllabusBody = "this is the syllabus body")
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Select ${course.name} course.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG,"Navigate to Syllabus Page. Assert that the syllabus body string is displayed, and there are no tabs yet.")
        courseBrowserPage.selectSyllabus()
        syllabusPage.assertNoTabs()
        syllabusPage.assertSyllabusBody("this is the syllabus body")

        Log.d(PREPARATION_TAG,"Seed an assignment for ${course.name} course.")
        val assignment = createAssignment(course, teacher)

        Log.d(PREPARATION_TAG,"Seed a quiz for ${course.name} course.")
        val quiz = createQuiz(course, teacher)

        Log.d(STEP_TAG,"Refresh the page. Navigate to 'Summary' tab. Assert that all of the items, so ${assignment.name} assignment and ${quiz.title} quiz are displayed.")
        syllabusPage.refresh()
        syllabusPage.selectSummaryTab()
        syllabusPage.assertItemDisplayed(assignment.name)
        syllabusPage.assertItemDisplayed(quiz.title)
    }

    private fun createQuiz(
        course: CourseApiModel,
        teacher: CanvasUserApiModel
    ) = QuizzesApi.createQuiz(
        QuizzesApi.CreateQuizRequest(
            courseId = course.id,
            withDescription = true,
            published = true,
            token = teacher.token,
            dueAt = 2.days.fromNow.iso8601
        )
    )

    private fun createAssignment(
        course: CourseApiModel,
        teacher: CanvasUserApiModel
    ) = AssignmentsApi.createAssignment(
        AssignmentsApi.CreateAssignmentRequest(
            courseId = course.id,
            teacherToken = teacher.token,
            submissionTypes = listOf(SubmissionType.ON_PAPER),
            dueAt = 1.days.fromNow.iso8601,
            withDescription = true
        )
    )
}