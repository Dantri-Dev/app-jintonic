package com.jintonic.app.core.compose

import com.jintonic.app.assessment.CognitiveAssessment
import com.jintonic.app.assessment.HealthAssessment
import com.jintonic.app.data.HealthView
import com.jintonic.app.data.ManualEntry
import com.jintonic.app.data.PvtSnapshot
import com.jintonic.app.data.SessionRecord
import com.jintonic.app.data.sensors.HrSource
import com.jintonic.app.data.sensors.VitalsSnapshot
import com.jintonic.app.pvt.PvtMode
import com.jintonic.app.pvt.Status
import com.jintonic.app.vitals.AlertLevel
import com.jintonic.app.vitals.BreathingGuide
import com.jintonic.app.vitals.HealthParameter
import com.jintonic.app.vitals.ParameterReading
import java.util.concurrent.TimeUnit

// Representative fixtures for @WearPreview only, so screens render without a ViewModel.
object PreviewData {

    fun healthView(overall: AlertLevel = AlertLevel.WARNING): HealthView = HealthView(
        snapshot = VitalsSnapshot(hrBpm = 72, spo2Pct = 96, hrSource = HrSource.SIMULATED),
        manual = ManualEntry(glucoseMgDl = 100, stress = 2, radiationMsvPerDay = 0.5, exerciseMinPerDay = 90),
        assessment = HealthAssessment(
            readings = listOf(
                ParameterReading(HealthParameter.HR, AlertLevel.NOMINAL),
                ParameterReading(HealthParameter.SPO2, AlertLevel.WARNING),
                ParameterReading(HealthParameter.GLUCOSE, AlertLevel.NOMINAL),
                ParameterReading(HealthParameter.STRESS, AlertLevel.WARNING),
                ParameterReading(HealthParameter.RADIATION, AlertLevel.CRITICAL),
                ParameterReading(HealthParameter.EXERCISE, AlertLevel.NOMINAL),
            ),
            readinessScore = 78,
            overallLevel = overall,
            triage = overall == AlertLevel.CRITICAL,
            breathingGuide = BreathingGuide(),
        ),
        minutesToCritical = null,
    )

    fun cognitive(
        status: Status = Status.GREEN,
        escalate: Boolean = false,
        mismatch: Boolean = false,
        sleep: Boolean = false,
    ): CognitiveAssessment = CognitiveAssessment(
        score = when (status) {
            Status.GREEN -> 86
            Status.YELLOW -> 62
            Status.RED -> 38
        },
        status = status,
        perceivedMismatch = mismatch,
        sleepWarning = sleep,
        advice = emptyList(),
        escalateToSurgeon = escalate,
    )

    fun sessions(): List<SessionRecord> = listOf(
        session(status = Status.GREEN, cognitiveScore = 88, agoMin = 30),
        session(status = Status.YELLOW, cognitiveScore = 64, agoMin = 180, pendingSync = true),
        session(status = Status.RED, cognitiveScore = 41, agoMin = 1_440, escalated = true, pendingSync = true),
    )

    private fun session(
        status: Status,
        cognitiveScore: Int,
        agoMin: Long,
        escalated: Boolean = false,
        pendingSync: Boolean = false,
    ): SessionRecord = SessionRecord(
        id = "preview-$cognitiveScore",
        timestampMs = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(agoMin),
        mode = PvtMode.STANDARD.name,
        pvt = PvtSnapshot(medianRtMs = 280.0, meanSpeed = 3.6, lapses = 1, falseStarts = 0, validTrials = 20),
        sleepHours = 7f,
        mood = 4,
        cognitiveScore = cognitiveScore,
        status = status.name,
        escalated = escalated,
        readinessScore = 78,
        overallLevel = AlertLevel.NOMINAL.name,
        hrBpm = 72,
        spo2Pct = 96,
        pendingSync = pendingSync,
    )
}
