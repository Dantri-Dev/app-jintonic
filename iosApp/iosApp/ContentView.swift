import SwiftUI

struct ContentView: View {
    var body: some View {
        VStack(spacing: 6) {
            Text("Jintonic")
                .font(.title3)
                .fontWeight(.semibold)
            Text("Ciao Daniele 👋")
                .font(.footnote)
                .foregroundStyle(.secondary)
            Text("Build di prova")
                .font(.caption2)
                .foregroundStyle(.tertiary)
        }
    }
}

#Preview {
    ContentView()
}
