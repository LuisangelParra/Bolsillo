import ProjectDescription

let project = Project(
    name: "Bolsillo",
    options: .options(
        developmentRegion: "es"
    ),
    packages: [
        .local(path: "Packages/Bolsillo"),
    ],
    targets: [
        .target(
            name: "Bolsillo",
            destinations: .iOS,
            product: .app,
            bundleId: "com.bolsillo.app",
            deploymentTargets: .iOS("17.0"),
            infoPlist: .extendingDefault(with: [
                "UILaunchScreen": [:],
                "CFBundleDisplayName": "Bolsillo",
            ]),
            sources: ["App/Sources/**"],
            resources: ["App/Resources/**"],
            dependencies: [
                .package(product: "BolsilloDomain"),
                .package(product: "BolsilloData"),
                .package(product: "BolsilloDesignSystem"),
                .package(product: "FeatureRecord"),
            ]
        ),
        .target(
            name: "BolsilloTests",
            destinations: .iOS,
            product: .unitTests,
            bundleId: "com.bolsillo.app.tests",
            deploymentTargets: .iOS("17.0"),
            sources: ["App/Tests/**"],
            dependencies: [
                .target(name: "Bolsillo"),
                .package(product: "BolsilloDomain"),
                .package(product: "BolsilloData"),
            ]
        ),
        .target(
            name: "BolsilloUITests",
            destinations: .iOS,
            product: .uiTests,
            bundleId: "com.bolsillo.app.uitests",
            deploymentTargets: .iOS("17.0"),
            sources: ["App/UITests/**"],
            dependencies: [
                .target(name: "Bolsillo"),
            ]
        ),
    ],
    schemes: [
        .scheme(
            name: "Bolsillo",
            shared: true,
            buildAction: .buildAction(targets: ["Bolsillo"]),
            testAction: .targets(["BolsilloTests", "BolsilloUITests"])
        ),
    ]
)
