// swift-tools-version: 5.10
import PackageDescription

// Architecture: FeatureRecord + BolsilloDesignSystem → BolsilloDomain ← BolsilloData
// BolsilloDesignSystem is presentation-only (no domain/data imports) — Article VIII.
// SQLCipher (T002): using standard GRDB product (system SQLite) for the swift test path.
// To enable encryption at rest, switch BolsilloData to GRDBCustomSQLite and provide a
// CSQLite target built from the SQLCipher amalgamation (SQLITE_HAS_CODEC). All key-mgmt
// code (KeychainKeyProvider) and passphrase-open stubs are already in place.
let package = Package(
    name: "Bolsillo",
    defaultLocalization: "es",
    // .macOS floor is for `swift test` host builds only; the shipped app is iOS-only.
    // macOS 14 required: @Observable / Observation framework (ships with Xcode 15+).
    platforms: [.iOS(.v17), .macOS(.v14)],
    products: [
        .library(name: "BolsilloDomain",      targets: ["BolsilloDomain"]),
        .library(name: "BolsilloData",         targets: ["BolsilloData"]),
        .library(name: "BolsilloDesignSystem", targets: ["BolsilloDesignSystem"]),
        .library(name: "FeatureRecord",        targets: ["FeatureRecord"]),
    ],
    dependencies: [
        .package(url: "https://github.com/groue/GRDB.swift.git", from: "6.29.0"),
    ],
    targets: [
        // MARK: Domain (pure Swift, no dependencies)
        .target(name: "BolsilloDomain"),

        // MARK: Data (GRDB, KeychainKeyProvider, repositories, seeds, AI stub)
        .target(
            name: "BolsilloData",
            dependencies: [
                "BolsilloDomain",
                .product(name: "GRDB", package: "GRDB.swift"),
            ]
        ),

        // MARK: Design System (presentation-only — zero domain/data imports)
        .target(
            name: "BolsilloDesignSystem",
            dependencies: [],
            resources: [.process("Resources")]
        ),

        // MARK: Feature: Record
        .target(
            name: "FeatureRecord",
            dependencies: [
                "BolsilloDomain",
                "BolsilloDesignSystem",
            ],
            resources: [.process("Resources")]
        ),

        // MARK: Tests
        .testTarget(
            name: "BolsilloDomainTests",
            dependencies: ["BolsilloDomain"]
        ),
        .testTarget(
            name: "BolsilloDataTests",
            dependencies: [
                "BolsilloData",
                .product(name: "GRDB", package: "GRDB.swift"),
            ]
        ),
        .testTarget(
            name: "BolsilloDesignSystemTests",
            dependencies: ["BolsilloDesignSystem"]
        ),
        .testTarget(
            name: "FeatureRecordTests",
            dependencies: [
                "FeatureRecord",
                "BolsilloDomain",
                "BolsilloData",
                .product(name: "GRDB", package: "GRDB.swift"),
            ]
        ),
    ]
)
