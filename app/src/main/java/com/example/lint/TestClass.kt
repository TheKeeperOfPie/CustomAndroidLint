package com.example.lint

import android.annotation.SuppressLint
import io.reactivex.Observable

class TestClass {

    fun subscribeValid() {
        Observable.just("")
            .startWith(Observable.just(""))
            .subscribe()
    }

    fun subscribeInvalid() {
        Observable.just("")
            .startWith { Observable.just("") }
            .subscribe()
    }

    @SuppressLint("RxJavaObservableSource")
    fun subscribeInvalidSuppressed() {
        Observable.just("")
            .startWith { Observable.just("") }
            .subscribe()
    }
}