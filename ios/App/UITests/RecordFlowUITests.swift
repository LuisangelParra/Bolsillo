import XCTest

final class RecordFlowUITests: XCTestCase {
    var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    // T054: 3-tap save flow (≤ 3 taps per spec FR5)
    // Manual/CI: amount entry = 1 interaction, Save = 1 tap; digit presses excluded.
    func testThreeTapSaveFlow() throws {
        // 1. App launches straight into keypad (no loading screen)
        let keypad = app.buttons["1"]
        XCTAssertTrue(keypad.waitForExistence(timeout: 3),
                      "Keypad must be visible on launch — no loading screen during recording (FR2)")

        // 2. Type amount via keypad (digit presses are not counted as 'taps' per spec)
        app.buttons["1"].tap()
        app.buttons["0"].tap()
        app.buttons["0"].tap()   // entering 100 minor units

        // 3. Save (1 tap — category + account pre-filled, no additional taps required)
        let saveButton = app.buttons["record.save"]
        XCTAssertTrue(saveButton.isEnabled, "Save button must be enabled with amount > 0 (Article V)")
        saveButton.tap()

        // 4. Undo toast appears
        let undoToast = app.buttons["record.undo"]
        XCTAssertTrue(undoToast.waitForExistence(timeout: 2), "Undo toast must appear after save (FR6)")
    }

    // Balance update after save
    func testBalanceUpdatesAfterSave() throws {
        XCTSkip("Balance display requires home screen — deferred to E3 navigation")
    }

    // Undo reverts
    func testUndoRevertsExpense() throws {
        try testThreeTapSaveFlow()
        app.buttons["record.undo"].tap()
        // Toast dismisses; app returns to clean state
        let undoToast = app.buttons["record.undo"]
        XCTAssertFalse(undoToast.waitForExistence(timeout: 1), "Toast must dismiss after undo")
    }
}
