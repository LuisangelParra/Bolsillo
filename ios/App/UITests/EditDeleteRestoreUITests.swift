import XCTest

// T073: edit recompute + delete→trash→restore round trip.
final class EditDeleteRestoreUITests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    // Edit/delete/restore UI requires a transaction list (home screen — E3).
    // These tests validate the use-case layer; full UI round-trip deferred to E3.
    func testEditDeleteRestoreRoundTrip() throws {
        XCTSkip("Transaction list UI deferred to E3 home screen feature. Domain round-trip covered by DeleteRestoreTests in BolsilloDomainTests.")
    }
}
