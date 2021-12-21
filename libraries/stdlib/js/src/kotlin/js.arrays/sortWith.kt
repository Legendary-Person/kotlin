/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.js

import kotlin.js.arrays.defineSortPolyfillForTypedArrays

@PublishedApi
@JsNativeImplementation(defineSortPolyfillForTypedArrays)
internal fun <T> Any.nativeSortWith(comparison: (a: T, b: T) -> Int): Unit {
    asDynamic().sort(comparison)
}
