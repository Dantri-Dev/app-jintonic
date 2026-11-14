import Foundation

// User-facing copy (Italian), mirroring androidApp res/values/strings.xml.
enum L {
    static let appName = "jintonic"

    // Actions
    static let actionDoTest = "Fai il test"
    static let actionConfirm = "Conferma"
    static let actionAllow = "Consenti"
    static let actionDone = "Fatto"
    static let actionStop = "Stop"
    static let actionQuickTest = "Test rapido 30 s"

    // Navigation / titles
    static let navHistory = "Storico"
    static let navBody = "Corpo"
    static let navParams = "Parametri"
    static let navEmergency = "Emergenza"
    static let titleReadiness = "Readiness"
    static let titleCognitive = "Salute"

    // Onboarding
    static let onboardingSubtitle = "Misura lucidità e parametri vitali per dirti se sei in grado di lavorare."
    static let onboardingPermission = "Serve l'accesso alla frequenza cardiaca. Se neghi, viene simulata."

    // Home
    static func streakDays(_ n: Int) -> String { n == 1 ? "🔥 \(n) giorno" : "🔥 \(n) giorni" }
    static func pendingSync(_ n: Int) -> String { "\(n) in coda per la Terra" }

    // Pre-test
    static let pretestTitle = "Sonno e umore"
    static func sleepHours(_ h: Float) -> String { String(format: "%.1f h", h) }
    static let sleepLess = "Meno sonno"
    static let sleepMore = "Più sonno"
    static func moodDesc(_ n: Int) -> String { "Umore \(n) su 5" }

    // Result
    static let headlineGreen = "Sei pronto. Buon lavoro."
    static let headlineYellow = "Fai una pausa di 10 minuti."
    static let headlineRed = "Evita compiti critici. Riposa."
    static let alertSurgeon = "Contatta il flight surgeon"
    static let alertMismatch = "Ti senti bene, ma i riflessi dicono il contrario"
    static let alertSleep = "Sonno insufficiente"
    static let sessionSaveFailed = "Impossibile salvare la sessione."

    // History
    static let historyEmpty = "Nessuna sessione. Fai il primo test dalla Home."
    static func historyCognitive(_ n: Int) -> String { "Salute \(n)" }
    static func historyReadiness(_ n: Int) -> String { "Readiness \(n)" }
    static let historyEscalation = "Escalation"
    static let historyPending = "In coda per la Terra"

    // Parameters
    static let loading = "Lettura in corso…"
    static let overallLevel = "Livello complessivo"
    static let hr = "HR"
    static let hrSimulated = "HR (simulata)"
    static let hrSimulatedOff = "HR (simulata · non al polso)"
    static let valueDash = "--"
    static func hrValue(_ bpm: Int) -> String { "\(bpm) bpm" }
    static let spo2Nominal = "SpO2 (nominale)"
    static func percent(_ n: Int) -> String { "\(n)%" }
    static let simSpo2Drop = "Simula calo SpO2"
    static let manualEntry = "Inserimento manuale"
    static let valueNotSet = "non inserito"
    static func stepperDecrease(_ s: String) -> String { "Diminuisci \(s)" }
    static func stepperIncrease(_ s: String) -> String { "Aumenta \(s)" }
    static func glucoseValue(_ n: Int) -> String { "\(n) mg/dL" }
    static func stressValue(_ n: Int) -> String { "\(n) / 5" }
    static func radiationValue(_ s: String) -> String { "\(s) mSv/g" }
    static func exerciseValue(_ n: Int) -> String { "\(n) min/g" }

    // Body
    static let bodyNa = "n.d."
    static let apparatusCardiovascular = "Cardiovascolare"
    static let apparatusRespiratory = "Respiratorio"
    static let apparatusMetabolic = "Metabolico"
    static let apparatusMusculoskeletal = "Muscoloscheletrico"
    static let apparatusPsychological = "Psicologico"

    // Emergency
    static let triageActive = "Triage attivo"
    static func paramsLevel(_ s: String) -> String { "Parametri: \(s)" }
    static func vitalsLine(_ hr: String, _ spo2: Int) -> String { "HR \(hr) bpm · SpO2 \(spo2)%" }
    static let breathingGuided = "Respirazione guidata"
    static let breatheIn = "Inspira"
    static let breatheOut = "Espira"

    // Alert levels
    static let levelNominal = "Nominale"
    static let levelWarning = "Attenzione"
    static let levelCritical = "Critico"

    // Health parameters
    static let paramGlucose = "Glicemia"
    static let paramStress = "Stress"
    static let paramRadiation = "Radiazioni"
    static let paramExercise = "Esercizio"
}
