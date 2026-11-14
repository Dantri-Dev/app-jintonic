import SwiftUI

// Label over value, centred: long labels never squeeze or split the value.
struct InfoRow: View {
    let label: String
    let value: String
    var valueColor: Color = FfdColors.onBackground

    var body: some View {
        VStack(spacing: 2) {
            Text(label)
                .font(.caption)
                .foregroundStyle(FfdColors.textSecondary)
                .multilineTextAlignment(.center)
            Text(value)
                .font(.title3.weight(.semibold))
                .foregroundStyle(valueColor)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
    }
}

// One-line explanation under a hero or at the foot of a list.
struct Hint: View {
    let text: String
    var color: Color = FfdColors.textSecondary

    init(_ text: String, color: Color = FfdColors.textSecondary) {
        self.text = text
        self.color = color
    }

    var body: some View {
        Text(text)
            .font(.caption)
            .foregroundStyle(color)
            .multilineTextAlignment(.center)
            .frame(maxWidth: .infinity)
            .padding(.horizontal, 8)
    }
}

// Empty / error state: an outlined SF Symbol over one explanatory line.
struct EmptyState: View {
    let systemImage: String
    let text: String
    var tint: Color = FfdColors.textSecondary

    var body: some View {
        VStack(spacing: 6) {
            Image(systemName: systemImage)
                .font(.system(size: 26))
                .foregroundStyle(tint)
            Hint(text, color: tint)
        }
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 12)
    }
}

// Section header matching the Wear ListHeader role.
struct SectionHeader: View {
    let title: String
    init(_ title: String) { self.title = title }
    var body: some View {
        Text(title)
            .font(.headline)
            .foregroundStyle(FfdColors.onBackground)
            .frame(maxWidth: .infinity)
            .padding(.top, 4)
    }
}
