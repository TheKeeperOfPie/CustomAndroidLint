@file:Suppress("FoldInitializerAndIfToElvis")

package com.example.lint

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.ULambdaExpression

class ExampleSourceDetector : Detector(), SourceCodeScanner {

    companion object {

        val ISSUE = Issue.create(
            id = "RxJavaObservableSource",
            briefDescription = "Do not use `ObservableSource` lambdas",
            explanation = "Using startWith(ObservableSource) with a lambda implicitly creates an ObservableSource",
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.ERROR,
            implementation = Implementation(
                ExampleSourceDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }

    override fun getApplicableMethodNames() = listOf("startWith")

    override fun visitMethod(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        super.visitMethod(context, node, method)

        // Not structure, so we can use the PsiMethod
        val parameterName = method.parameterList.parameters.firstOrNull()
            ?.type
            ?.canonicalText
            ?: return

        // Only methods that take an ObservableSource
        if (!parameterName.startsWith("io.reactivex.ObservableSource")) {
            return
        }

        // Operate on the UAST node
        val valueArgument = node.valueArguments.firstOrNull() ?: return

        // Only if created with a lambda (as this implicitly creates the ObservableSource)
        if (valueArgument !is ULambdaExpression) {
            return
        }

        context.report(
            issue = ISSUE,
            scope = node.receiver,
            location = context.getLocation(node),
            message = "${node.methodName} should not use a lambda, as this creates $parameterName",
            quickfixData = LintFix.create()
                .replace()
                .apply {
                    if (valueArgument.valueParameters.isNotEmpty()) {
                        // Regex for .methodCall { parameter -> value }
                        pattern("${node.methodName}(\\s*\\Q{\\E.*\\s*->\\s*((?s:.)*)(?<=\\S)\\s*\\Q}\\E)")
                    } else {
                        // Regex for .methodCall { value }
                        pattern("${node.methodName}(\\s*\\Q{\\E\\s*((?s:.)*)(?<=\\S)\\s*\\Q}\\E)")
                    }
                }
                .with("""(\k<2>)""".trimMargin())
                .autoFix()
                .build()
        )
    }
}