import SwiftUI

// Position of each apparatus on the figure, as fractions of the canvas (x, y).
private let anchors: [FfdApparatus: CGPoint] = [
    .psychological: CGPoint(x: 0.5, y: 0.11),
    .cardiovascular: CGPoint(x: 0.56, y: 0.32),
    .respiratory: CGPoint(x: 0.42, y: 0.29),
    .metabolic: CGPoint(x: 0.5, y: 0.46),
    .radiation: CGPoint(x: 0.25, y: 0.38),
    .musculoskeletal: CGPoint(x: 0.44, y: 0.78),
]

struct BodyView: View {
    @EnvironmentObject private var store: SessionStore
    @EnvironmentObject private var vitals: VitalsMonitor

    var body: some View {
        let view = HealthEvaluation.evaluate(engine: Engine.make(), snapshot: vitals.snapshot,
                                             manual: store.manual, lastSession: store.sessions.last)
        let levels = view.apparatusLevels()
        ScrollView {
            VStack(spacing: 10) {
                SectionHeader(L.navBody)
                BodyFigure(levels: levels)
                    .frame(width: 90, height: 140)
                ForEach(FfdApparatus.allCases, id: \.self) { apparatus in
                    let level = levels[apparatus]
                    InfoRow(label: apparatus.label,
                            value: level?.label ?? L.bodyNa,
                            valueColor: level?.color ?? FfdColors.textSecondary)
                }
            }
            .padding(.horizontal, 10)
            .padding(.top, 4)
        }
        .background(FfdColors.background)
    }
}

private struct BodyFigure: View {
    let levels: [FfdApparatus: FfdLevel]

    var body: some View {
        Canvas { ctx, size in
            drawSilhouette(ctx, size)
            for (apparatus, anchor) in anchors {
                let color = levels[apparatus]?.color ?? FfdColors.textSecondary
                let center = CGPoint(x: anchor.x * size.width, y: anchor.y * size.height)
                ctx.fill(circle(center, 9), with: .color(color.opacity(0.35)))
                ctx.fill(circle(center, 5), with: .color(color))
            }
        }
        .accessibilityLabel(summary)
    }

    private var summary: String {
        levels.map { "\($0.key.label) \($0.value.label)" }.joined(separator: ", ")
    }

    private func circle(_ center: CGPoint, _ r: CGFloat) -> Path {
        Path(ellipseIn: CGRect(x: center.x - r, y: center.y - r, width: r * 2, height: r * 2))
    }

    private func drawSilhouette(_ ctx: GraphicsContext, _ size: CGSize) {
        let w = size.width, h = size.height
        let fill = FfdColors.textSecondary.opacity(0.25)
        let radius = w * 0.08
        ctx.fill(circle(CGPoint(x: w * 0.5, y: h * 0.11), w * 0.13), with: .color(fill))
        func box(_ x: CGFloat, _ y: CGFloat, _ bw: CGFloat, _ bh: CGFloat) {
            ctx.fill(Path(roundedRect: CGRect(x: x, y: y, width: bw, height: bh), cornerRadius: radius), with: .color(fill))
        }
        box(w * 0.3, h * 0.21, w * 0.4, h * 0.36)   // torso
        box(w * 0.14, h * 0.22, w * 0.13, h * 0.32) // left arm
        box(w * 0.73, h * 0.22, w * 0.13, h * 0.32) // right arm
        box(w * 0.32, h * 0.58, w * 0.16, h * 0.4)  // left leg
        box(w * 0.52, h * 0.58, w * 0.16, h * 0.4)  // right leg
        ctx.stroke(circle(CGPoint(x: w * 0.5, y: h * 0.11), w * 0.13),
                   with: .color(FfdColors.textSecondary.opacity(0.4)), lineWidth: 1)
    }
}
