import SwiftUI
import Shared

struct EmergencyView: View {
    let onQuickTest: () -> Void

    @EnvironmentObject private var store: SessionStore
    @EnvironmentObject private var vitals: VitalsMonitor
    @StateObject private var vm = EmergencyViewModel()

    var body: some View {
        let view = HealthEvaluation.evaluate(engine: Engine.make(), snapshot: vitals.snapshot,
                                             manual: store.manual, lastSession: store.sessions.last)
        let guide = view.assessment.breathingGuide ?? defaultBreathingGuide()
        ScrollView {
            VStack(spacing: 12) {
                SectionHeader(L.navEmergency)
                statusSection(view)
                BreathingItem(phase: vm.phase, guide: guide,
                              onStart: { vm.startBreathing(guide) },
                              onStop: { vm.stopBreathing() })
            }
            .padding(.horizontal, 10)
            .padding(.top, 4)
        }
        .safeAreaInset(edge: .bottom) {
            FfdButton(L.actionQuickTest, action: onQuickTest)
                .padding(.horizontal, 12)
        }
        .background(FfdColors.background)
        .onDisappear { vm.stopBreathing() }
    }

    private func statusSection(_ view: HealthViewData) -> some View {
        let a = view.assessment
        let overall = FfdLevel(a.overallLevel)
        let hr = view.snapshot.hrBpm.map(String.init) ?? L.valueDash
        return VStack(spacing: 8) {
            Text(a.triage ? L.triageActive : L.paramsLevel(overall.label))
                .font(.headline)
                .foregroundStyle(a.triage ? FfdColors.accentStrong : overall.color)
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
            Hint(L.vitalsLine(hr, view.snapshot.spo2Pct))
        }
    }
}

private struct BreathingItem: View {
    let phase: BreathPhase
    let guide: BreathingGuide
    let onStart: () -> Void
    let onStop: () -> Void

    private let minScale: CGFloat = 0.6

    var body: some View {
        if phase == .idle {
            FfdButton(L.breathingGuided, kind: .tonal, action: onStart)
        } else {
            let inhaling = phase == .inhale
            VStack(spacing: 10) {
                ZStack {
                    Circle()
                        .strokeBorder(FfdColors.primary, lineWidth: 6)
                        .frame(width: 100, height: 100)
                        .scaleEffect(inhaling ? 1 : minScale)
                        .animation(.easeInOut(duration: Double(inhaling ? guide.inhaleSec : guide.exhaleSec)),
                                   value: phase)
                    Text(inhaling ? L.breatheIn : L.breatheOut)
                        .font(.headline)
                        .foregroundStyle(FfdColors.onBackground)
                }
                .frame(width: 110, height: 110)
                FfdButton(L.actionStop, kind: .tonal, action: onStop)
            }
        }
    }
}
