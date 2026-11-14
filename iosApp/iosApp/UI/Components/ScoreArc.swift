import SwiftUI

// Full-screen score ring that follows the bezel as an expressive wave, score
// centred. Hand-drawn: there is no native wavy-progress primitive. Screen hero.
struct ScoreArc: View {
    let score: Int?
    let label: String
    var caption: String = ""
    let color: Color

    private let gapDeg: Double = 72
    private let wavelengthDeg: Double = 14
    private let amp: CGFloat = FfdSizes.waveAmplitude
    private var startDeg: Double { 90 + gapDeg / 2 }
    private var sweepDeg: Double { 360 - gapDeg }

    var body: some View {
        ZStack {
            Canvas { ctx, size in
                let stroke = FfdSizes.ringStroke
                let radius = min(size.width, size.height) / 2 - stroke / 2 - amp
                let center = CGPoint(x: size.width / 2, y: size.height / 2)

                let track = arcPath(radius: radius, amp: 0, center: center, sweep: sweepDeg)
                ctx.stroke(track, with: .color(color.opacity(0.2)),
                           style: StrokeStyle(lineWidth: stroke, lineCap: .round))

                let progress = (Double(score ?? 0) / 100.0).clamped(0, 1)
                // A zero score is a danger state: fill the whole ring as one bright alert.
                let fill = progress == 0 ? 1 : progress
                let wave = arcPath(radius: radius, amp: amp, center: center, sweep: sweepDeg * fill)
                ctx.stroke(wave, with: .color(color),
                           style: StrokeStyle(lineWidth: stroke, lineCap: .round))
            }
            .padding(2)

            VStack(spacing: 0) {
                Text(score.map(String.init) ?? "--")
                    .font(.system(size: 40, weight: .bold, design: .rounded))
                    .foregroundStyle(FfdColors.onBackground)
                Text(label.uppercased())
                    .font(.caption2)
                    .foregroundStyle(FfdColors.textSecondary)
                if !caption.isEmpty {
                    Text(caption)
                        .font(.caption2)
                        .foregroundStyle(FfdColors.accentSoft)
                }
            }
        }
    }

    private func arcPath(radius: CGFloat, amp: CGFloat, center: CGPoint, sweep: Double) -> Path {
        var path = Path()
        let steps = max(Int(sweep), 2)
        for i in 0...steps {
            let deg = startDeg + sweep * (Double(i) / Double(steps))
            let offsetDeg = deg - startDeg
            let r = radius + amp * CGFloat(sin(offsetDeg / wavelengthDeg * 2 * .pi))
            let rad = deg * .pi / 180
            let point = CGPoint(x: center.x + r * CGFloat(cos(rad)),
                                y: center.y + r * CGFloat(sin(rad)))
            if i == 0 { path.move(to: point) } else { path.addLine(to: point) }
        }
        return path
    }
}

extension Comparable {
    func clamped(_ lo: Self, _ hi: Self) -> Self { min(max(self, lo), hi) }
}
