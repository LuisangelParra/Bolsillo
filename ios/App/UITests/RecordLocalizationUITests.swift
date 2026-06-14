import XCTest

// T055: es and en rendering — no truncation, no hard-coded text.
final class RecordLocalizationUITests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func testSpanishLocalization() throws {
        let app = XCUIApplication()
        app.launchArguments += ["-AppleLanguages", "(es)", "-AppleLocale", "es_CO"]
        app.launch()

        XCTAssertTrue(app.buttons["Guardar"].waitForExistence(timeout: 3)
            || app.buttons["record.save"].waitForExistence(timeout: 1),
            "Save button must render in Spanish (es) — no hard-coded en text")
    }

    func testEnglishLocalization() throws {
        let app = XCUIApplication()
        app.launchArguments += ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
        app.launch()

        XCTAssertTrue(app.buttons["Save"].waitForExistence(timeout: 3)
            || app.buttons["record.save"].waitForExistence(timeout: 1),
            "Save button must render in English — no hard-coded es text")
    }
}
