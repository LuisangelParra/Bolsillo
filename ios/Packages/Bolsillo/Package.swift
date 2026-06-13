// swift-tools-version: 5.10
import PackageDescription

// Three library products wired together (clean architecture):
//   FeatureRecord ─▶ BolsilloDomain ◀─ BolsilloData
// BolsilloDomain has no dependencies (ports + value types only).
let package = Package(
    name: "Bolsillo",
    defaultLocalization: "es",
    // macOS floor is only so `swift test` can build the SwiftUI target on the host;
    // the app itself ships iOS-only (see ios/Project.swift).
    platforms: [.iOS(.v17), .macOS(.v13)],
    products: [
        .library(name: "BolsilloDomain", targets: ["BolsilloDomain"]),
        .library(name: "BolsilloData", targets: ["BolsilloData"]),
        .library(name: "FeatureRecord", targets: ["FeatureRecord"]),
    ],
    dependencies: [
        // Persistence (wired, not yet used). SQLCipher provides encryption-at-rest and
        // will be enabled via GRDB's SQLCipher build when the schema lands (see Sources/BolsilloData).
        .package(url: "https://github.com/groue/GRDB.swift.git", from: "6.29.0"),
    ],
    targets: [
        .target(name: "BolsilloDomain"),
        .target(
            name: "BolsilloData",
            dependencies: [
                "BolsilloDomain",
                .product(name: "GRDB", package: "GRDB.swift"),
            ]
        ),
        .target(
            name: "FeatureRecord",
            dependencies: ["BolsilloDomain"]
        ),
        .testTarget(
            name: "BolsilloDomainTests",
            dependencies: ["BolsilloDomain"]
        ),
    ]
)
