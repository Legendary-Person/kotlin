/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeSubstitutor
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

val JVM_INLINE_ANNOTATION_FQ_NAME = FqName("kotlin.jvm.JvmInline")
val JVM_INLINE_ANNOTATION_CLASS_ID = ClassId.topLevel(JVM_INLINE_ANNOTATION_FQ_NAME)

// FIXME: DeserializedClassDescriptor in reflection do not have @JvmInline annotation, that we
// FIXME: would like to check as well.
fun DeclarationDescriptor.isInlineClass(): Boolean =
    this is ClassDescriptor && (isInline || isValue && (unsubstitutedPrimaryConstructor?.valueParameters?.size?.let { it <= 1 } ?: true))

fun DeclarationDescriptor.isValueClass(): Boolean =
    this is ClassDescriptor && isValue

fun DeclarationDescriptor.isInlineOrValueClass(): Boolean = isInlineClass() || isValueClass()

fun KotlinType.unsubstitutedUnderlyingType(): KotlinType? =
    constructor.declarationDescriptor.safeAs<ClassDescriptor>()?.inlineClassRepresentation?.underlyingType

fun KotlinType.unsubstitutedUnderlyingTypes(): List<KotlinType> {
    val declarationDescriptor = constructor.declarationDescriptor.safeAs<ClassDescriptor>() ?: return emptyList()
    return when {
        declarationDescriptor.isInlineClass() -> listOfNotNull(unsubstitutedUnderlyingType())
        declarationDescriptor.isValueClass() ->
            declarationDescriptor.unsubstitutedPrimaryConstructor?.valueParameters?.map { it.type } ?: emptyList()
        else -> emptyList()
    }
}


fun KotlinType.isInlineClassType(): Boolean = constructor.declarationDescriptor?.isInlineClass() ?: false

fun KotlinType.substitutedUnderlyingType(): KotlinType? =
    unsubstitutedUnderlyingType()?.let { TypeSubstitutor.create(this).substitute(it, Variance.INVARIANT) }

fun KotlinType.substitutedUnderlyingTypes(): List<KotlinType?> =
    unsubstitutedUnderlyingTypes().map { TypeSubstitutor.create(this).substitute(it, Variance.INVARIANT) }

fun KotlinType.isRecursiveInlineOrValueClassType(): Boolean =
    isRecursiveInlineOrValueClassTypeInner(hashSetOf())

private fun KotlinType.isRecursiveInlineOrValueClassTypeInner(visited: HashSet<ClassifierDescriptor>): Boolean {
    val descriptor = constructor.declarationDescriptor?.original ?: return false

    if (!visited.add(descriptor)) return true

    val old = visited.toHashSet()

    class NestedHashSet : HashSet<ClassifierDescriptor>(old) {
        override fun add(element: ClassifierDescriptor): Boolean = super.add(element).also { visited.add(element) }
    }

    return when (descriptor) {
        is ClassDescriptor ->
            descriptor.isInlineOrValueClass() && unsubstitutedUnderlyingTypes().any {
                it.isRecursiveInlineOrValueClassTypeInner(NestedHashSet())
            }

        is TypeParameterDescriptor ->
            descriptor.upperBounds.any { it.isRecursiveInlineOrValueClassTypeInner(visited) }

        else -> false
    }
}

fun KotlinType.isNullableUnderlyingType(): Boolean {
    if (!isInlineClassType()) return false
    val underlyingType = unsubstitutedUnderlyingType() ?: return false

    return TypeUtils.isNullableType(underlyingType)
}

fun CallableDescriptor.isGetterOfUnderlyingPropertyOfInlineClass() =
    this is PropertyGetterDescriptor && correspondingProperty.isUnderlyingPropertyOfInlineClass()

fun VariableDescriptor.isUnderlyingPropertyOfInlineClass(): Boolean =
    extensionReceiverParameter == null &&
            (containingDeclaration as? ClassDescriptor)?.inlineClassRepresentation?.underlyingPropertyName == this.name
