package com.jintonic.app.assessment

import com.jintonic.app.advice.AdviceRules
import com.jintonic.app.baseline.BaselineCalculator
import com.jintonic.app.pvt.PreTestAnswers
import com.jintonic.app.pvt.PvtMetrics
import com.jintonic.app.pvt.PvtResult
import com.jintonic.app.pvt.PvtTrial
import com.jintonic.app.pvt.StimulusGenerator
import com.jintonic.app.scoring.CognitiveScoring
import com.jintonic.app.scoring.ReadinessScoring
import com.jintonic.app.vitals.BreathingGuide
import com.jintonic.app.vitals.ManualInputs
import com.jintonic.app.vitals.Thresholds
import com.jintonic.app.vitals.Triage
import com.jintonic.app.vitals.Vitals

// Public entry point of the shared engine. Deterministic and offline.
interface FitForDutyEngine {
    fun nextStimulusDelayMs(): Long
    fun evaluate(trials: List<PvtTrial>): PvtResult
    fun assessCognitive(result: PvtResult, answers: PreTestAnswers, history: List<PvtResult>): CognitiveAssessment
    fun assessHealth(vitals: Vitals, manual: ManualInputs): HealthAssessment
}

// Seeded for reproducible stimulus timing within a session.
class DefaultFitForDutyEngine(seed: Long) : FitForDutyEngine {

    private val stimulus = StimulusGenerator(seed)

    override fun nextStimulusDelayMs(): Long = stimulus.nextDelayMs()

    override fun evaluate(trials: List<PvtTrial>): PvtResult = PvtMetrics.evaluate(trials)

    override fun assessCognitive(
        result: PvtResult,
        answers: PreTestAnswers,
        history: List<PvtResult>,
    ): CognitiveAssessment {
        val baseline = BaselineCalculator.fromHistory(history)
        val score = CognitiveScoring.score(result, baseline)
        val status = CognitiveScoring.status(score)
        val pastStatuses = history.map { CognitiveScoring.status(CognitiveScoring.score(it, baseline)) }
        return CognitiveAssessment(
            score = score,
            status = status,
            perceivedMismatch = AdviceRules.perceivedMismatch(status, answers),
            sleepWarning = AdviceRules.sleepWarning(answers),
            advice = AdviceRules.adviceFor(status, answers),
            escalateToSurgeon = AdviceRules.shouldEscalate(status, pastStatuses),
        )
    }

    override fun assessHealth(vitals: Vitals, manual: ManualInputs): HealthAssessment {
        val readings = Thresholds.classify(vitals, manual)
        val triage = Triage.needsTriage(readings, vitals)
        return HealthAssessment(
            readings = readings,
            readinessScore = ReadinessScoring.score(vitals, manual),
            overallLevel = Triage.overallLevel(readings),
            triage = triage,
            breathingGuide = if (triage) BreathingGuide() else null,
        )
    }
}
