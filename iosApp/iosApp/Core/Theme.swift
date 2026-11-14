import SwiftUI
import Shared

// Colour roles come from the shared engine (com.jintonic.app.design), the single
// source for Wear OS and watchOS. ARGB Int64 values are unpacked into SwiftUI Color.
extension Color {
    init(argb: Int64) {
        let a = Double((argb >> 24) & 0xFF) / 255.0
        let r = Double((argb >> 16) & 0xFF) / 255.0
        let g = Double((argb >> 8) & 0xFF) / 255.0
        let b = Double(argb & 0xFF) / 255.0
        self.init(.sRGB, red: r, green: g, blue: b, opacity: a)
    }
}

enum FfdColors {
    private static let roles = ColorRoles.shared

    static let background = Color(argb: roles.BACKGROUND)
    static let onBackground = Color(argb: roles.ON_BACKGROUND)
    static let primary = Color(argb: roles.PRIMARY)
    static let secondary = Color(argb: roles.SECONDARY)
    static let levelHigh = Color(argb: roles.LEVEL_HIGH)
    static let levelMid = Color(argb: roles.LEVEL_MID)
    static let levelLow = Color(argb: roles.LEVEL_LOW)
    static let accentSoft = Color(argb: roles.ACCENT_SOFT)
    static let accentStrong = Color(argb: roles.ACCENT_STRONG)
    static let textSecondary = Color(argb: roles.TEXT_SECONDARY)
    static let surface = Color(argb: roles.SURFACE)
    static let error = Color(argb: Palette.Semantic.shared.ERROR)
}

enum FfdSizes {
    static let ring: CGFloat = 108
    static let ringStroke: CGFloat = 8
    static let waveAmplitude: CGFloat = 4
    static let stimulus: CGFloat = 76
}

extension FfdStatus {
    var color: Color {
        switch self {
        case .green: return FfdColors.levelHigh
        case .yellow: return FfdColors.levelMid
        case .red: return FfdColors.error
        }
    }
}

extension FfdLevel {
    var color: Color {
        switch self {
        case .nominal: return FfdColors.levelHigh
        case .warning: return FfdColors.levelMid
        case .critical: return FfdColors.levelLow
        }
    }

    var label: String {
        switch self {
        case .nominal: return L.levelNominal
        case .warning: return L.levelWarning
        case .critical: return L.levelCritical
        }
    }
}
