import SwiftUI
import Shared

struct ResultView: View {
    @ObservedObject var vm: TestFlowViewModel
    let onDone: () -> Void

    var body: some View {
        content
            .safeAreaInset(edge: .bottom) {
                FfdButton(L.actionDone, action: onDone)
                    .padding(.horizontal, 12)
            }
            .background(FfdColors.background)
            .navigationBarBackButtonHidden(true)
    }

    @ViewBuilder
    private var content: some View {
        switch vm.state {
        case let .success(outcome):
            successView(outcome.cognitive)
        case .failure(let message):
            EmptyState(systemImage: "exclamationmark.triangle", text: message, tint: FfdColors.accentStrong)
                .frame(maxHeight: .infinity)
        default:
            ProgressView().frame(maxHeight: .infinity)
        }
    }

    private func successView(_ cognitive: CognitiveAssessment) -> some View {
        let status = FfdStatus(cognitive.status)
        return ScrollView {
            VStack(spacing: 10) {
                ScoreArc(score: Int(cognitive.score),
                         label: L.titleCognitive,
                         color: status.color)
                    .frame(height: 180)

                VStack(spacing: 6) {
                    Hint(headline(status), color: FfdColors.onBackground)
                    if let alert = alert(cognitive) {
                        Hint(alert.text, color: alert.color)
                    }
                }
                .padding(.bottom, 8)
            }
            .padding(.horizontal, 10)
        }
    }

    private func headline(_ status: FfdStatus) -> String {
        switch status {
        case .green: return L.headlineGreen
        case .yellow: return L.headlineYellow
        case .red: return L.headlineRed
        }
    }

    private func alert(_ cognitive: CognitiveAssessment) -> (text: String, color: Color)? {
        if cognitive.escalateToSurgeon { return (L.alertSurgeon, FfdColors.accentStrong) }
        if cognitive.perceivedMismatch { return (L.alertMismatch, FfdColors.accentSoft) }
        if cognitive.sleepWarning { return (L.alertSleep, FfdColors.accentSoft) }
        return nil
    }
}
