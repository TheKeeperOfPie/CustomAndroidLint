package com.example.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API

class ExampleIssueRegistry : IssueRegistry() {

    override val api = CURRENT_API

    override val issues = listOf(
        ExampleSourceDetector.ISSUE
    )

}