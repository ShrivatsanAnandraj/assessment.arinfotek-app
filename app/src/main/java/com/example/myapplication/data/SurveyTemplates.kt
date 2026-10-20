package com.example.myapplication.data

/** Built-in survey templates, ported from the web app's api/surveys.js TEMPLATES. */
object SurveyTemplates {

    val templates: Map<String, SurveyTemplate> = mapOf(
        "Template 1" to SurveyTemplate(
            name = "IT Training Feedback Survey - Set 1",
            questions = listOf(
                SurveyQuestion(
                    "How would you rate your overall experience with the Power BI course?",
                    listOf("Poor", "Fair", "Good", "Very Good", "Excellent")
                ),
                SurveyQuestion(
                    "How would you rate the quality, relevance, and usefulness of the topics covered in the course?",
                    listOf("Poor", "Fair", "Good", "Very Good", "Excellent")
                ),
                SurveyQuestion(
                    "How much has your knowledge of Power BI improved after completing the course?",
                    listOf("Very little", "A little", "Moderate", "A lot", "Very significantly")
                ),
                SurveyQuestion(
                    "How useful were the hands-on exercises and practical activities for understanding Power BI?",
                    listOf("Not useful", "Slightly useful", "Moderately useful", "Useful", "Very useful")
                ),
                SurveyQuestion(
                    "How would you rate the trainer's subject knowledge and expertise?",
                    listOf("Poor", "Fair", "Good", "Very Good", "Excellent")
                ),
                SurveyQuestion(
                    "How clearly did the trainer explain the concepts and demonstrate the practical steps?",
                    listOf("Not clear", "Slightly clear", "Moderately clear", "Clear", "Very clear")
                ),
                SurveyQuestion(
                    "How effectively did the trainer address questions, doubts, and difficulties during the sessions?",
                    listOf("Not effective", "Slightly effective", "Moderately effective", "Effective", "Very effective")
                ),
                SurveyQuestion(
                    "How confident are you now in using Power BI for real-world data analysis and dashboard development?",
                    listOf("Not confident", "Slightly confident", "Moderately confident", "Confident", "Very confident")
                ),
                SurveyQuestion(
                    "How likely are you to recommend this Power BI course to your friends, colleagues, or classmates?",
                    listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), nps = true
                ),
                SurveyQuestion(
                    "How satisfied are you with the overall course, trainer, learning experience, and practical exposure?",
                    listOf("Very dissatisfied", "Dissatisfied", "Neutral", "Satisfied", "Very satisfied")
                )
            )
        ),
        "Template 2" to SurveyTemplate(
            name = "IT Training Feedback Survey - Set 2",
            questions = listOf(
                SurveyQuestion("Quality of Practice Exercises. Rate the quality and real-life usefulness of the hands-on lab exercises."),
                SurveyQuestion("Were You Ready for This Course. I had enough background knowledge before starting this course."),
                SurveyQuestion("Difficulty of the Labs. How would you describe the difficulty level of the practice labs?"),
                SurveyQuestion("Help With Problems. Rate how helpful the trainer was when you faced problems or errors during the labs."),
                SurveyQuestion("Balance of Theory and Practice. The balance between lectures and hands-on practice was good."),
                SurveyQuestion("Confidence After Training. How confident do you feel solving problems on your own after this training?"),
                SurveyQuestion("Usefulness of Sample Code. Rate how useful the sample code, scripts, or files given to you were."),
                SurveyQuestion("Did the Course Meet Its Goals. This training covered everything it promised to teach."),
                SurveyQuestion("Hardest Lab or Exercise. Which lab or exercise was the hardest for you to complete, and why?"),
                SurveyQuestion("More Practice Tools Needed. What other practice tools or lab setups would help you learn better?")
            )
        ),
        "Template 3" to SurveyTemplate(
            name = "IT Training Feedback Survey - Set 3",
            questions = listOf(
                SurveyQuestion("Trainer's Communication. Rate how clearly the trainer spoke and how well they kept the class interested."),
                SurveyQuestion("Time Given for Exercises. Was there enough time to comfortably finish each hands-on activity?"),
                SurveyQuestion("Up-to-date Technology. The tools and technical ideas taught match what is used in the industry today."),
                SurveyQuestion("Group Work & Discussions. Rate how useful the Q&A sessions, group discussions, and interaction with classmates were."),
                SurveyQuestion("Would You Recommend This Course. How likely are you to recommend this training to a friend or classmate?"),
                SurveyQuestion("Quality of Notes & Guides. The notes and reference guides given will be useful to you later on."),
                SurveyQuestion("Overall Learning Experience. Rate your overall experience with the course structure, content, and teaching."),
                SurveyQuestion("Support After the Course. What kind of extra help or follow-up support would help you continue learning?"),
                SurveyQuestion("Most Important Thing You Learned. What is the one most important skill or idea you learned from this course?"),
                SurveyQuestion("Topics You Want Next. What other IT, Cloud, DevOps, or Software topics would you like to learn next?")
            )
        )
    )
}

data class SurveyTemplate(val name: String, val questions: List<SurveyQuestion>)
