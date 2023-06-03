package com.detpros.unrealkotlin.corrections


import kotlin.reflect.KProperty


/**
 *  Corrections Provider
 *
 * @author IvanEOD ( 5/26/2023 at 3:03 PM EST )
 */
sealed interface CorrectionsProvider {
    operator fun getValue( thisRef: Any?, property: KProperty<*> ): Corrections
}