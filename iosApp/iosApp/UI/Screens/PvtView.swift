import SwiftUI

struct PvtView: View {
    @ObservedObject var vm: TestFlowViewModel
    let onFinished: () -> Void
    @StateObject private var runner: PvtRunner
    @State private var pulse = false

    init(vm: TestFlowViewModel, onFinished: @escaping () -> Void) {
        self.vm = vm
        self.onFinished = onFinished
        _runner = StateObject(wrappedValue: PvtRunner(durationSec: Int(vm.mode.durationSec),
                                                      nextDelay: { vm.nextStimulusDelayMs() }))
    }

    var body: some View {
        ZStack {
            FfdColors.background.ignoresSafeArea()

            Text(formatClock(runner.remainingMs))
                .font(.footnote)
                .foregroundStyle(FfdColors.textSecondary)
                .frame(maxHeight: .infinity, alignment: .top)
                .padding(.top, 8)

            if runner.stimulusVisible {
                stimulus
            }
        }
        .contentShape(Rectangle())
        .onTapGesture {
            if runner.stimulusVisible { Haptics.stimulus() }
            runner.tap()
        }
        .onChange(of: runner.stimulusVisible) { _, visible in
            if visible { Haptics.stimulus() }
        }
        .navigationBarBackButtonHidden(true)
        .task {
            let trials = await runner.run()
            vm.finish(trials)
            onFinished()
        }
    }

    // Solid ball that pulses slowly to draw the eye, with a tap glyph.
    private var stimulus: some View {
        ZStack {
            Circle()
                .fill(FfdColors.primary)
                .frame(width: FfdSizes.stimulus, height: FfdSizes.stimulus)
                .scaleEffect(pulse ? 1.1 : 0.86)
                .animation(.easeInOut(duration: 0.85).repeatForever(autoreverses: true), value: pulse)
            Image(systemName: "hand.tap.fill")
                .font(.system(size: 30))
                .foregroundStyle(FfdColors.onBackground)
        }
        .onAppear { pulse = true }
        .onDisappear { pulse = false }
    }

    private func formatClock(_ ms: Int64) -> String {
        let totalSec = Int((ms + 999) / 1000)
        return String(format: "%d:%02d", totalSec / 60, totalSec % 60)
    }
}
