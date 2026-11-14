package com.jintonic.app.assessment

import com.jintonic.app.advice.AdviceCode
import com.jintonic.app.pvt.Status
import com.jintonic.app.vitals.AlertLevel
import com.jintonic.app.vitals.BreathingGuide
import com.jintonic.app.vitals.ParameterReading

// Cognitive outcome of one session (PLAN.md 5.12).
data class CognitiveAssessment(
    val score: Int,
    val status: Status,
    val perceivedMismatch: Boolean,
    val sleepWarning: Boolean,
    val advice: List<AdviceCode>,
    val escalateToSurgeon: Boolean,
)

// Vitals/health outcome (parameters.md).
data class HealthAssessment(
    val readings: List<ParameterReading>,
    val readinessScore: Int,
    val overallLevel: AlertLevel,
    val triage: Boolean,
    val breathingGuide: BreathingGuide?,
)
