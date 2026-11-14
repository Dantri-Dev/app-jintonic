import SwiftUI

private let dateFormatter: DateFormatter = {
    let f = DateFormatter()
    f.dateFormat = "dd/MM HH:mm"
    return f
}()

struct HistoryView: View {
    @EnvironmentObject private var store: SessionStore

    var body: some View {
        let sessions = store.sessions.reversed().map { $0 } // newest first
        ScrollView {
            VStack(spacing: 8) {
                SectionHeader(L.navHistory)
                if sessions.isEmpty {
                    EmptyState(systemImage: "clock.arrow.circlepath", text: L.historyEmpty)
                        .padding(.top, 8)
                } else {
                    ForEach(sessions) { SessionCard(session: $0) }
                }
            }
            .padding(.horizontal, 10)
            .padding(.top, 4)
        }
        .background(FfdColors.background)
    }
}

private struct SessionCard: View {
    let session: SessionRecord

    var body: some View {
        HStack(spacing: 8) {
            Circle()
                .fill(session.ffdStatus.color)
                .frame(width: 10, height: 10)
            VStack(alignment: .leading, spacing: 2) {
                Text(L.historyCognitive(session.cognitiveScore))
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(FfdColors.onBackground)
                Text(dateFormatter.string(from: session.date))
                    .font(.caption2)
                    .foregroundStyle(FfdColors.textSecondary)
                if let readiness = session.readinessScore {
                    Text(L.historyReadiness(readiness))
                        .font(.caption2)
                        .foregroundStyle(FfdColors.textSecondary)
                }
                if session.escalated {
                    Text(L.historyEscalation)
                        .font(.caption2)
                        .foregroundStyle(FfdColors.accentStrong)
                }
                if session.pendingSync {
                    Text(L.historyPending)
                        .font(.caption2)
                        .foregroundStyle(FfdColors.accentSoft)
                }
            }
            Spacer(minLength: 0)
        }
        .padding(10)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(FfdColors.surface, in: RoundedRectangle(cornerRadius: 14))
    }
}
