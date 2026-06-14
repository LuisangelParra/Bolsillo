import SwiftUI

/// Renders a rounded square with category foreground / container colors and an SF Symbol icon.
public struct CategoryIconTile: View {
    public enum TileSize {
        case sm, md, lg

        public var side: CGFloat {
            switch self {
            case .sm: return 38
            case .md: return 44
            case .lg: return 54
            }
        }

        public var cornerRadius: CGFloat {
            switch self {
            case .sm: return BolsilloRadius.shared.iconTileSm
            case .md: return BolsilloRadius.shared.iconTile
            case .lg: return BolsilloRadius.shared.iconTileLg
            }
        }

        public var iconSize: CGFloat {
            switch self {
            case .sm: return 17
            case .md: return 20
            case .lg: return 25
            }
        }
    }

    public let icon: String
    public let fg: Color
    public let container: Color
    public let tileSize: TileSize

    public init(icon: String, fg: Color, container: Color, size: TileSize = .md) {
        self.icon      = icon
        self.fg        = fg
        self.container = container
        self.tileSize  = size
    }

    public var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: tileSize.cornerRadius, style: .continuous)
                .fill(container)
                .frame(width: tileSize.side, height: tileSize.side)
            Image(systemName: icon)
                .font(.system(size: tileSize.iconSize, weight: .semibold))
                .foregroundStyle(fg)
        }
    }
}
