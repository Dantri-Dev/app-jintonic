import Foundation
import Shared

enum BreathPhase { case idle, inhale, exhale }

// Guided breathing countermeasure: cadence from the engine, pulses on the wrist.
@MainActor
final class EmergencyViewModel: ObservableObject {
    @Published private(set) var phase: BreathPhase = .idle
    private var task: Task<Void, Never>?

    func startBreathing(_ guide: BreathingGuide) {
        guard task == nil else { return }
        task = Task { [weak self] in
            while !Task.isCancelled {
                guard let self else { return }
                self.phase = .inhale
                Haptics.inhale()
                try? await Task.sleep(nanoseconds: UInt64(guide.inhaleSec) * 1_000_000_000)
                if Task.isCancelled { return }
                self.phase = .exhale
                Haptics.exhale()
                try? await Task.sleep(nanoseconds: UInt64(guide.exhaleSec) * 1_000_000_000)
            }
        }
    }

    func stopBreathing() {
        task?.cancel()
        task = nil
        phase = .idle
    }
}
