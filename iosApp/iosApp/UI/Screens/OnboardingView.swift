import SwiftUI

// Explains the app and asks to continue. Heart rate is simulated on the wrist
// until a live HealthKit source is granted.
struct OnboardingView: View {
    let onContinue: () -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 8) {
                Text(L.appName)
                    .font(.title3.weight(.bold))
                    .foregroundStyle(FfdColors.primary)
                Hint(L.onboardingSubtitle)
                Hint(L.onboardingPermission, color: FfdColors.accentSoft)
            }
            .padding(.horizontal, 8)
            .padding(.top, 8)
        }
        .safeAreaInset(edge: .bottom) {
            FfdButton(L.actionAllow, action: onContinue)
                .padding(.horizontal, 12)
        }
        .background(FfdColors.background)
    }
}
