import SwiftUI

private let faces = ["😣", "🙁", "😐", "🙂", "😄"]
private let faceOffOpacity = 0.3

// Sleep stepper and mood faces, confirmed with a bottom button.
struct PreTestView: View {
    @ObservedObject var vm: TestFlowViewModel
    let onConfirm: () -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 12) {
                Text(L.pretestTitle)
                    .font(.headline)
                    .foregroundStyle(FfdColors.onBackground)

                sleepRow
                facesRow
            }
            .padding(.horizontal, 10)
            .padding(.top, 6)
        }
        .safeAreaInset(edge: .bottom) {
            FfdButton(L.actionConfirm, action: onConfirm)
                .padding(.horizontal, 12)
        }
        .background(FfdColors.background)
    }

    private var sleepRow: some View {
        HStack(spacing: 10) {
            FfdIconButton(glyph: "−", accessibilityLabel: L.sleepLess) {
                vm.setSleep(max(0, vm.form.sleepHours - AppConstants.sleepStepHours))
            }
            Text(L.sleepHours(vm.form.sleepHours))
                .font(.title3.weight(.semibold))
                .foregroundStyle(FfdColors.onBackground)
                .frame(minWidth: 64)
            FfdIconButton(glyph: "+", accessibilityLabel: L.sleepMore) {
                vm.setSleep(min(AppConstants.sleepMaxHours, vm.form.sleepHours + AppConstants.sleepStepHours))
            }
        }
    }

    private var facesRow: some View {
        HStack(spacing: 4) {
            ForEach(Array(faces.enumerated()), id: \.offset) { index, face in
                let mood = index + 1
                Text(face)
                    .font(.system(size: 24))
                    .opacity(mood == vm.form.mood ? 1 : faceOffOpacity)
                    .ffdTappable { vm.setMood(mood) }
                    .accessibilityLabel(L.moodDesc(mood))
            }
        }
        .padding(.top, 4)
    }
}
