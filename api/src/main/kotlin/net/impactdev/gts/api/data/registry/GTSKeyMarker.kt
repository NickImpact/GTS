package net.impactdev.gts.api.data.registry

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(RetentionPolicy.RUNTIME)
annotation class GTSKeyMarker(vararg val value: String)