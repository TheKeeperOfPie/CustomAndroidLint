package com.example.lint

import com.android.tools.lint.checks.infrastructure.TestFiles
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class ExampleLintTest {

    companion object {
        private val OBSERVABLE_STUB = TestFiles.java(
            """
                    package io.reactivex;

                    import io.reactivex.Observer;

                    public class Observable<T> implements ObservableSource<T> {

                        static Observable<String> just(String string) {
                            return new Observable<>();
                        }

                        public final Observable<T> startWith(ObservableSource<? extends T> other) {
                            return this;
                        }

                        public void subscribe(Observer<? super T> observer) {

                        }
                    }
                """
        )

        private val OBSERVABLE_SOURCE_STUB = TestFiles.java(
            """
                    package io.reactivex;

                    public interface ObservableSource<T> {
                        void subscribe(Observer<? super T> observer);
                    }
                """
        )

        private val OBSERVER_STUB = TestFiles.java(
            """
                    package io.reactivex;

                    public interface Observer<T> {

                    }
                """
        )
    }

    @Test
    fun `test valid startWith`() {
        lint()
            .files(
                OBSERVABLE_STUB,
                OBSERVABLE_SOURCE_STUB,
                OBSERVER_STUB,
                kotlin(
                    """
                            package com.example.lint

                            import io.reactivex.Observable

                            class TestClass {

                                init {
                                    Observable.just(null)
                                        .startWith(Observable.just(null))
                                }
                            }
                            """
                )
            )
            .issues(ExampleSourceDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun `test invalid startWith`() {
        lint()
            .files(
                OBSERVABLE_STUB,
                OBSERVABLE_SOURCE_STUB,
                OBSERVER_STUB,
                kotlin(
                    """
                            package com.example.lint

                            import io.reactivex.Observable

                            class TestClass {

                                init {
                                    Observable.just(null)
                                        .startWith { Observable.just(null) }
                                }
                            }
                            """
                )
            )
            .issues(ExampleSourceDetector.ISSUE)
            .run()
            .expect(
                """
                    src/com/example/lint/TestClass.kt:9: Error: startWith should not use a lambda, as this creates io.reactivex.ObservableSource<? extends T> [RxJavaObservableSource]
                                                        Observable.just(null)
                                                        ^
                    1 errors, 0 warnings
                """
            )
            .verifyFixes()
            .checkFix(
                null, kotlin(
                    """
                            package com.example.lint

                            import io.reactivex.Observable

                            class TestClass {

                                init {
                                    Observable.just(null)
                                        .startWith(Observable.just(null))
                                }
                            }
                            """
                )
            )
            .expectFixDiffs(
                """
                    Fix for src/com/example/lint/TestClass.kt line 8: Replace with (\k<2>):
                    @@ -10 +10
                    -                                         .startWith { Observable.just(null) }
                    +                                         .startWith(Observable.just(null))
                """
            )
    }

    @Test
    fun `test invalid startWith with argument`() {
        lint()
            .files(
                OBSERVABLE_STUB,
                OBSERVABLE_SOURCE_STUB,
                OBSERVER_STUB,
                kotlin(
                    """
                            package com.example.lint

                            import io.reactivex.Observable

                            class TestClass {

                                init {
                                    Observable.just(null)
                                        .startWith { observer -> Observable.just(null) }
                                }
                            }
                            """
                )
            )
            .issues(ExampleSourceDetector.ISSUE)
            .run()
            .expect(
                """
                    src/com/example/lint/TestClass.kt:9: Error: startWith should not use a lambda, as this creates io.reactivex.ObservableSource<? extends T> [RxJavaObservableSource]
                                                        Observable.just(null)
                                                        ^
                    1 errors, 0 warnings
                """
            )
            .verifyFixes()
            .checkFix(
                null, kotlin(
                    """
                            package com.example.lint

                            import io.reactivex.Observable

                            class TestClass {

                                init {
                                    Observable.just(null)
                                        .startWith(Observable.just(null))
                                }
                            }
                            """
                )
            )
            .expectFixDiffs(
                """
                    Fix for src/com/example/lint/TestClass.kt line 9: Replace with (\k<2>):
                    @@ -10 +10
                    -                                         .startWith { observer -> Observable.just(null) }
                    +                                         .startWith(Observable.just(null))
                """
            )
    }
}
