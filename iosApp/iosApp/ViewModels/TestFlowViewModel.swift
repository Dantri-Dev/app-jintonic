import Foundation
import Shared

struct TestOutcome {
    let cognitive: CognitiveAssessment
    let result: PvtResult
}

// One view model for the whole flow: Pre-test -> PVT -> Result.
@MainActor
final class TestFlowViewModel: ObservableObject {
    enum State {
        case idle, loading
        case success(TestOutcome)
        case failure(String)
    }

    struct Form {
        var sleepHours: Float = AppConstants.sleepDefaultHours
        var mood: Int = AppConstants.moodDefault
    }

    @Published var form = Form()
    @Published private(set) var state: State = .idle
    private(set) var mode: PvtMode = .demo

    private var engine: FitForDutyEngine = Engine.make()
    private let store: SessionStore
    private let vitals: VitalsMonitor

    init(store: SessionStore, vitals: VitalsMonitor) {
        self.store = store
        self.vitals = vitals
    }

    func begin(_ mode: PvtMode) {
        self.mode = mode
        engine = Engine.make()
        state = .idle
    }

    func setSleep(_ hours: Float) { form.sleepHours = hours }
    func setMood(_ mood: Int) { form.mood = mood }

    func nextStimulusDelayMs() -> Int64 { engine.nextStimulusDelayMs() }

    func finish(_ trials: [PvtTrial]) {
        state = .loading
        assessAndSave(trials)
    }

    private func assessAndSave(_ trials: [PvtTrial]) {
        let past = store.sessions
        let result = engine.evaluate(trials: trials)
        let cognitive = engine.assessCognitive(
            result: result,
            answers: PreTestAnswers(sleepHours: form.sleepHours, mood: Int32(form.mood)),
            history: past.map { $0.pvt.toResult() }
        )
        let snapshot = vitals.snapshot
        let health = HealthEvaluation.evaluate(engine: engine, snapshot: snapshot,
                                               manual: store.manual, lastSession: past.last).assessment
        let isRed = FfdStatus(cognitive.status) == .red
        let record = SessionRecord(
            id: UUID().uuidString,
            timestampMs: Int64(Date().timeIntervalSince1970 * 1000),
            mode: mode.name,
            pvt: PvtSnapshot.from(result),
            sleepHours: form.sleepHours,
            mood: form.mood,
            cognitiveScore: Int(cognitive.score),
            status: cognitive.status.name,
            escalated: cognitive.escalateToSurgeon,
            readinessScore: Int(health.readinessScore),
            overallLevel: health.overallLevel.name,
            hrBpm: snapshot.hrBpm,
            spo2Pct: snapshot.spo2Pct,
            // Outbox: red results and escalations wait for the next link to Earth.
            pendingSync: isRed || cognitive.escalateToSurgeon
        )
        store.append(record)
        state = .success(TestOutcome(cognitive: cognitive, result: result))
    }
}
