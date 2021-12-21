/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:JsQualifier("Math")
package kotlin.js

import kotlin.js.math.defineTaylorNBound

@PublishedApi
@JsName("tanh")
@JsNativeImplementation("""
if (typeof Math.tanh === "undefined") {
    $defineTaylorNBound
    Math.tanh = function(x){
        if (Math.abs(x) < taylor_n_bound) {
            var result = x;
            if (Math.abs(x) > taylor_2_bound) {
                result -= (x * x * x) / 3;
            }
            return result;
        }
        else {
            var a = Math.exp(+x), b = Math.exp(-x);
            return a === Infinity ? 1 : b === Infinity ? -1 : (a - b) / (a + b);
        }
    };
}
""")
internal external fun nativeTanh(value: Double): Double