import Foundation
import Shared

// Live health picture: vitals snapshot + manual inputs run through the shared engine.
struct HealthViewData {
    let snapshot: VitalsSnapshot
    let manual: ManualEntry
    let assessment: HealthAssessment
    let minutesToCritical: Double?

    func level(for parameter: FfdParameter) -> FfdLevel? {
        for reading in assessment.readings {
            if reading.parameter.name == parameter.rawValue { return FfdLevel(reading.level) }
        }
        return nil
    }

    // Worst level per apparatus (null = no data), for the body map.
    func apparatusLevels() -> [FfdApparatus: FfdLevel] {
        var result: [FfdApparatus: FfdLevel] = [:]
        for reading in assessment.readings {
            guard let app = FfdApparatus(reading.parameter.apparatus.name) else { continue }
            let level = FfdLevel(reading.level)
            if let existing = result[app], existing.severity >= level.severity { continue }
            result[app] = level
        }
        return result
    }
}

// Glue between live vitals, persisted manual inputs and the shared engine.
enum HealthEvaluation {
    static func evaluate(engine: FitForDutyEngine,
                         snapshot: VitalsSnapshot,
                         manual: ManualEntry,
                         lastSession: SessionRecord?) -> HealthViewData {
        let inputs = ManualInputs(
            glucoseMgDl: manual.glucoseMgDl.kotlinInt,
            stress: manual.stress.kotlinInt,
            radiationMsvPerDay: manual.radiationMsvPerDay.kotlinDouble,
            exerciseMinPerDay: manual.exerciseMinPerDay.kotlinInt,
            sleepHours: lastSession.map { KotlinFloat(float: $0.sleepHours) },
            mood: (lastSession?.mood).kotlinInt
        )
        let assessment = engine.assessHealth(vitals: snapshot.toVitals(), manual: inputs)
        return HealthViewData(snapshot: snapshot, manual: manual, assessment: assessment,
                              minutesToCritical: timeToCritical(snapshot, assessment))
    }

    // 0 if HR or SpO2 is already critical, else the nearest linear projection (nil = stable).
    private static func timeToCritical(_ snapshot: VitalsSnapshot, _ assessment: HealthAssessment) -> Double? {
        let alreadyCritical = assessment.readings.contains {
            FfdLevel($0.level) == .critical && ($0.parameter.name == "HR" || $0.parameter.name == "SPO2")
        }
        if alreadyCritical { return 0 }

        var candidates: [Double] = []
        if moved(snapshot.hrSeries, minDelta: AppConstants.hrTrendMinDelta),
           let m = Triage.shared.minutesToCritical(series: toTimed(snapshot.hrSeries),
                                                   criticalThreshold: AppConstants.hrCriticalHigh)?.doubleValue {
            candidates.append(m)
        }
        if moved(snapshot.spo2Series, minDelta: AppConstants.spo2TrendMinDelta),
           let m = Triage.shared.minutesToCritical(series: toTimed(snapshot.spo2Series),
                                                   criticalThreshold: AppConstants.spo2CriticalLow)?.doubleValue {
            candidates.append(m)
        }
        return candidates.min()
    }

    private static func moved(_ series: [TimedPoint], minDelta: Double) -> Bool {
        guard let hi = series.map(\.value).max(), let lo = series.map(\.value).min() else { return false }
        return (hi - lo) >= minDelta
    }

    private static func toTimed(_ series: [TimedPoint]) -> [TimedValue] {
        series.map { TimedValue(timeMs: $0.timeMs, value: $0.value) }
    }
}
