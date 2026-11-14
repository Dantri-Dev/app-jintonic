import Foundation
import Shared

// Swift-native mirrors of the shared engine enums, bridged by their Kotlin name.
// Keeps the UI off the boxed Objective-C enum singletons.

enum FfdStatus {
    case green, yellow, red

    init(_ status: Status) {
        switch status.name {
        case "GREEN": self = .green
        case "YELLOW": self = .yellow
        default: self = .red
        }
    }
}

enum FfdLevel {
    case nominal, warning, critical

    init(_ level: AlertLevel) {
        switch level.name {
        case "NOMINAL": self = .nominal
        case "WARNING": self = .warning
        default: self = .critical
        }
    }

    // Worst-of ordering for apparatus roll-up.
    var severity: Int {
        switch self {
        case .nominal: return 0
        case .warning: return 1
        case .critical: return 2
        }
    }
}

enum FfdApparatus: CaseIterable {
    case cardiovascular, respiratory, metabolic, radiation, musculoskeletal, psychological

    init?(_ name: String) {
        switch name {
        case "CARDIOVASCULAR": self = .cardiovascular
        case "RESPIRATORY": self = .respiratory
        case "METABOLIC": self = .metabolic
        case "RADIATION": self = .radiation
        case "MUSCULOSKELETAL": self = .musculoskeletal
        case "PSYCHOLOGICAL": self = .psychological
        default: return nil
        }
    }

    var label: String {
        switch self {
        case .cardiovascular: return L.apparatusCardiovascular
        case .respiratory: return L.apparatusRespiratory
        case .metabolic: return L.apparatusMetabolic
        case .radiation: return L.paramRadiation
        case .musculoskeletal: return L.apparatusMusculoskeletal
        case .psychological: return L.apparatusPsychological
        }
    }
}

// Which shared HealthParameter maps to a reading, matched by Kotlin name.
enum FfdParameter: String {
    case hr = "HR"
    case spo2 = "SPO2"
    case exercise = "EXERCISE"
    case glucose = "GLUCOSE"
    case stress = "STRESS"
    case radiation = "RADIATION"
    case sleep = "SLEEP"
    case mood = "MOOD"
}

// Kotlin default arguments do not generate a no-arg initializer, so build the
// guide explicitly from the engine's own cadence constants.
func defaultBreathingGuide() -> BreathingGuide {
    let c = Constants.shared
    return BreathingGuide(breathsPerMin: c.BREATHS_PER_MIN, inhaleSec: c.INHALE_SEC, exhaleSec: c.EXHALE_SEC)
}

// Boxing helpers for the nullable Kotlin primitives the engine takes/returns.
extension Optional where Wrapped == Int {
    var kotlinInt: KotlinInt? { map { KotlinInt(int: Int32($0)) } }
}

extension Optional where Wrapped == Double {
    var kotlinDouble: KotlinDouble? { map { KotlinDouble(double: $0) } }
}

extension Optional where Wrapped == Float {
    var kotlinFloat: KotlinFloat? { map { KotlinFloat(float: $0) } }
}
