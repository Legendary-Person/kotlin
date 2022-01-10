/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.config

import org.jetbrains.kotlin.load.java.JavaTypeEnhancementState

val JavaTypeEnhancementState.Companion.DEFAULT get() = getDefault(LanguageVersion.LATEST_STABLE.toKotlinVersion())
