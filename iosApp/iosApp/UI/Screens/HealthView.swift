import SwiftUI
import Shared

// Live parameters: vitals + manual entry. The "params" destination.
struct HealthView: View {
    @EnvironmentObject private var store: SessionStore
    @EnvironmentObject private var vitals: VitalsMonitor

    var body: some View {
        let view = HealthEvaluation.evaluate(engine: Engine.make(), snapshot: vitals.snapshot,
                                             manual: store.manual, lastSession: store.sessions.last)
        ScrollView {
            VStack(spacing: 12) {
                SectionHeader(L.navParams)
                vitalsSection(view)
                manualSection(view)
            }
            .padding(.horizontal, 10)
            .padding(.top, 4)
        }
        .background(FfdColors.background)
    }

    private func vitalsSection(_ view: HealthViewData) -> some View {
        let readiness = Int(view.assessment.readinessScore)
        let readinessStatus = FfdStatus(CognitiveScoring.shared.status(score: Int32(readiness)))
        let overall = FfdLevel(view.assessment.overallLevel)
        return VStack(spacing: 12) {
            InfoRow(label: L.titleReadiness, value: "\(readiness)", valueColor: readinessStatus.color)
            InfoRow(label: L.overallLevel, value: overall.label, valueColor: overall.color)
            InfoRow(label: hrLabel(view.snapshot.hrSource),
                    value: view.snapshot.hrBpm.map(L.hrValue) ?? L.valueDash,
                    valueColor: view.level(for: .hr)?.color ?? FfdColors.onBackground)
            InfoRow(label: L.spo2Nominal,
                    value: L.percent(view.snapshot.spo2Pct),
                    valueColor: view.level(for: .spo2)?.color ?? FfdColors.onBackground)
            Toggle(L.simSpo2Drop, isOn: Binding(
                get: { view.snapshot.crashActive },
                set: { active in Haptics.tap(); vitals.setCrash(active) }))
                .font(.caption)
                .tint(FfdColors.primary)
        }
    }

    private func manualSection(_ view: HealthViewData) -> some View {
        let m = view.manual
        return VStack(spacing: 12) {
            SectionHeader(L.manualEntry)
            StepperLine(label: L.paramGlucose,
                        value: m.glucoseMgDl.map(L.glucoseValue),
                        level: view.level(for: .glucose)) { delta in
                var e = m
                e.glucoseMgDl = ((m.glucoseMgDl ?? AppConstants.glucoseDefault) + delta * AppConstants.glucoseStep)
                    .clamped(0, AppConstants.glucoseMax)
                store.saveManual(e)
            }
            StepperLine(label: L.paramStress,
                        value: m.stress.map(L.stressValue),
                        level: view.level(for: .stress)) { delta in
                var e = m
                e.stress = ((m.stress ?? AppConstants.scaleDefault) + delta).clamped(1, AppConstants.scaleMax)
                store.saveManual(e)
            }
            StepperLine(label: L.paramRadiation,
                        value: m.radiationMsvPerDay.map { L.radiationValue(trimmed($0)) },
                        level: view.level(for: .radiation)) { delta in
                var e = m
                e.radiationMsvPerDay = stepRadiation(m.radiationMsvPerDay, delta)
                store.saveManual(e)
            }
            StepperLine(label: L.paramExercise,
                        value: m.exerciseMinPerDay.map(L.exerciseValue),
                        level: view.level(for: .exercise)) { delta in
                var e = m
                e.exerciseMinPerDay = ((m.exerciseMinPerDay ?? AppConstants.exerciseDefault) + delta * AppConstants.exerciseStep)
                    .clamped(0, AppConstants.exerciseMax)
                store.saveManual(e)
            }
        }
    }

    private func hrLabel(_ source: HrSource) -> String {
        switch source {
        case .sensor: return L.hr
        case .simulated: return L.hrSimulated
        case .simulatedOffBody: return L.hrSimulatedOff
        }
    }

    private func stepRadiation(_ current: Double?, _ delta: Int) -> Double {
        let next = (current ?? AppConstants.radiationDefault) + Double(delta) * AppConstants.radiationStep
        return (max(0, next) * 10).rounded() / 10
    }

    private func trimmed(_ v: Double) -> String {
        v == v.rounded() ? String(Int(v)) : String(v)
    }
}

private struct StepperLine: View {
    let label: String
    let value: String?
    let level: FfdLevel?
    let onStep: (Int) -> Void

    var body: some View {
        VStack(spacing: 4) {
            Text(label)
                .font(.caption)
                .foregroundStyle(FfdColors.textSecondary)
            HStack(spacing: 10) {
                FfdIconButton(glyph: "−", accessibilityLabel: L.stepperDecrease(label)) { onStep(-1) }
                Text(value ?? L.valueNotSet)
                    .font(.title3.weight(.semibold))
                    .foregroundStyle(level?.color ?? FfdColors.onBackground)
                    .frame(minWidth: 80)
                FfdIconButton(glyph: "+", accessibilityLabel: L.stepperIncrease(label)) { onStep(1) }
            }
        }
    }
}
