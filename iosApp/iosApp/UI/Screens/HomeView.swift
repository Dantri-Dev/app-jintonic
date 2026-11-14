import SwiftUI

struct HomeView: View {
    let onStartTest: () -> Void
    let onNavigate: (Route) -> Void

    @EnvironmentObject private var store: SessionStore
    @EnvironmentObject private var vitals: VitalsMonitor

    var body: some View {
        let ui = makeHomeUi(sessions: store.sessions, manual: store.manual, snapshot: vitals.snapshot)
        ScrollView {
            VStack(spacing: 10) {
                ScoreArc(score: ui.readiness,
                         label: L.titleReadiness,
                         caption: ui.streakDays > 0 ? L.streakDays(ui.streakDays) : "",
                         color: ui.status.color)
                    .frame(height: 180)

                menu(pendingSync: ui.pendingSync)
            }
            .padding(.horizontal, 10)
        }
        .safeAreaInset(edge: .bottom) {
            FfdButton(L.actionDoTest, action: onStartTest)
                .padding(.horizontal, 12)
        }
        .background(FfdColors.background)
        .navigationBarTitleDisplayMode(.inline)
    }

    private func menu(pendingSync: Int) -> some View {
        VStack(spacing: 6) {
            FfdButton(L.navHistory, kind: .tonal) { onNavigate(.history) }
            FfdButton(L.navBody, kind: .tonal) { onNavigate(.body) }
            FfdButton(L.navParams, kind: .tonal) { onNavigate(.params) }
            FfdButton(L.navEmergency, kind: .destructive) { onNavigate(.emergency) }
            if pendingSync > 0 {
                Hint(L.pendingSync(pendingSync), color: FfdColors.accentSoft)
            }
        }
        .padding(.bottom, 8)
    }
}
