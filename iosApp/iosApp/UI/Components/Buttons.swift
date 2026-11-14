import SwiftUI

// Every tappable goes through these helpers: springy press bounce, a 300 ms
// debounce, and a haptic on each accepted tap — never a bare Button action.
private let debounceInterval: TimeInterval = 0.3
private let pressScale: CGFloat = 0.92
private let bounce = Animation.spring(response: 0.3, dampingFraction: 0.45)

struct FilledButtonStyle: ButtonStyle {
    var fill: Color
    var foreground: Color
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.body.weight(.semibold))
            .foregroundStyle(foreground)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 10)
            .background(fill, in: Capsule())
            .scaleEffect(configuration.isPressed ? pressScale : 1)
            .animation(bounce, value: configuration.isPressed)
    }
}

struct TonalButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.body.weight(.medium))
            .foregroundStyle(FfdColors.onBackground)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 10)
            .background(FfdColors.primary.opacity(0.22), in: Capsule())
            .scaleEffect(configuration.isPressed ? pressScale : 1)
            .animation(bounce, value: configuration.isPressed)
    }
}

enum FfdButtonStyleKind {
    case primary, tonal, destructive

    var fill: Color {
        switch self {
        case .primary: return FfdColors.primary
        case .tonal: return .clear
        case .destructive: return FfdColors.accentStrong
        }
    }
    var foreground: Color {
        switch self {
        case .destructive: return FfdColors.background
        default: return FfdColors.onBackground
        }
    }
}

// Debounced + haptic button. Bounce/background come from the button style.
struct FfdButton<Label: View>: View {
    var kind: FfdButtonStyleKind = .primary
    let action: () -> Void
    @ViewBuilder var label: () -> Label
    @State private var lastTap = Date.distantPast

    var body: some View {
        Button(action: gated, label: label)
            .modifier(StyleModifier(kind: kind))
    }

    private func gated() {
        let now = Date()
        guard now.timeIntervalSince(lastTap) >= debounceInterval else { return }
        lastTap = now
        Haptics.tap()
        action()
    }

    private struct StyleModifier: ViewModifier {
        let kind: FfdButtonStyleKind
        func body(content: Content) -> some View {
            switch kind {
            case .tonal:
                content.buttonStyle(TonalButtonStyle())
            default:
                content.buttonStyle(FilledButtonStyle(fill: kind.fill, foreground: kind.foreground))
            }
        }
    }
}

extension FfdButton where Label == Text {
    init(_ title: String, kind: FfdButtonStyleKind = .primary, action: @escaping () -> Void) {
        self.init(kind: kind, action: action) { Text(title) }
    }
}

// Circular stepper / icon button for +/- controls.
struct FfdIconButton: View {
    let glyph: String
    let accessibilityLabel: String
    let action: () -> Void
    @State private var lastTap = Date.distantPast

    var body: some View {
        Button {
            let now = Date()
            guard now.timeIntervalSince(lastTap) >= debounceInterval else { return }
            lastTap = now
            Haptics.tap()
            action()
        } label: {
            Text(glyph)
                .font(.title3.weight(.semibold))
                .foregroundStyle(FfdColors.onBackground)
                .frame(width: 36, height: 36)
                .background(FfdColors.primary.opacity(0.22), in: Circle())
        }
        .buttonStyle(IconBounceStyle())
        .accessibilityLabel(accessibilityLabel)
    }

    private struct IconBounceStyle: ButtonStyle {
        func makeBody(configuration: Configuration) -> some View {
            configuration.label
                .scaleEffect(configuration.isPressed ? pressScale : 1)
                .animation(bounce, value: configuration.isPressed)
        }
    }
}

// Bounce + debounce + haptic for arbitrary tappables (mood faces).
struct TappableModifier: ViewModifier {
    let action: () -> Void
    @State private var lastTap = Date.distantPast

    func body(content: Content) -> some View {
        Button {
            let now = Date()
            guard now.timeIntervalSince(lastTap) >= debounceInterval else { return }
            lastTap = now
            Haptics.tap()
            action()
        } label: { content }
        .buttonStyle(PlainBounceStyle())
    }

    private struct PlainBounceStyle: ButtonStyle {
        func makeBody(configuration: Configuration) -> some View {
            configuration.label
                .scaleEffect(configuration.isPressed ? pressScale : 1)
                .animation(bounce, value: configuration.isPressed)
        }
    }
}

extension View {
    func ffdTappable(action: @escaping () -> Void) -> some View {
        modifier(TappableModifier(action: action))
    }
}
