import SwiftUI
import Shared

enum Route: Hashable {
    case history, body, params, emergency
    case preTest, pvt, result
}

struct ContentView: View {
    @EnvironmentObject private var store: SessionStore
    @EnvironmentObject private var vitals: VitalsMonitor
    @EnvironmentObject private var testVM: TestFlowViewModel
    @State private var path: [Route] = []
    @State private var onboarded = false

    var body: some View {
        Group {
            if onboarded {
                NavigationStack(path: $path) {
                    HomeView(onStartTest: { startTest(.demo, withPreTest: true) },
                             onNavigate: { path.append($0) })
                        .navigationDestination(for: Route.self, destination: destination)
                }
            } else {
                OnboardingView(onContinue: {
                    store.hasOnboarded = true
                    onboarded = true
                })
            }
        }
        .onAppear {
            onboarded = store.hasOnboarded
            vitals.start()
        }
    }

    @ViewBuilder
    private func destination(_ route: Route) -> some View {
        switch route {
        case .history: HistoryView()
        case .body: BodyView()
        case .params: HealthView()
        case .emergency: EmergencyView(onQuickTest: { startTest(.triage, withPreTest: false) })
        case .preTest: PreTestView(vm: testVM, onConfirm: { path.append(.pvt) })
        case .pvt: PvtView(vm: testVM, onFinished: { path = [.result] })
        case .result: ResultView(vm: testVM, onDone: { path = [] })
        }
    }

    private func startTest(_ mode: PvtMode, withPreTest: Bool) {
        testVM.begin(mode)
        path.append(withPreTest ? .preTest : .pvt)
    }
}
