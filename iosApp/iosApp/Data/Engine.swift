import Foundation
import Shared

// Fresh seed per session: reproducible within a run, varied across runs.
enum Engine {
    static func make() -> FitForDutyEngine {
        DefaultFitForDutyEngine(seed: Int64(Date().timeIntervalSince1970 * 1000))
    }
}

struct HomeUi {
    let readiness: Int
    let status: FfdStatus
    let streakDays: Int
    let pendingSync: Int
    let hasSessions: Bool
}

func makeHomeUi(sessions: [SessionRecord], manual: ManualEntry, snapshot: VitalsSnapshot) -> HomeUi {
    let health = HealthEvaluation.evaluate(engine: Engine.make(), snapshot: snapshot,
                                           manual: manual, lastSession: sessions.last)
    let readiness = Int(health.assessment.readinessScore)
    let status = FfdStatus(CognitiveScoring.shared.status(score: Int32(readiness)))
    return HomeUi(readiness: readiness, status: status, streakDays: streak(sessions),
                  pendingSync: sessions.filter { $0.pendingSync }.count, hasSessions: !sessions.isEmpty)
}

// Consecutive days with at least one session, ending today (or yesterday if not tested yet today).
func streak(_ sessions: [SessionRecord], today: Date = Date()) -> Int {
    let cal = Calendar.current
    let days = Set(sessions.map { cal.startOfDay(for: $0.date) })
    let todayStart = cal.startOfDay(for: today)
    var cursor = days.contains(todayStart) ? todayStart : cal.date(byAdding: .day, value: -1, to: todayStart)!
    var count = 0
    while days.contains(cursor) {
        count += 1
        cursor = cal.date(byAdding: .day, value: -1, to: cursor)!
    }
    return count
}
