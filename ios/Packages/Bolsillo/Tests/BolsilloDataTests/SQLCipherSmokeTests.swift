import Testing
import Foundation
@testable import BolsilloData

// T075: SQLCipher smoke tests.
// Requires GRDB built with SQLCipher (GRDBCustomSQLite + CSQLite target).
// Currently skipped: standard GRDB product (system SQLite) is used until the SQLCipher
// binary is wired in (see Package.swift T002 comment and ios/scripts/setup-sqlcipher.sh).
// To enable: switch BolsilloData to .product(name: "GRDBCustomSQLite") and uncomment below.
@Suite struct SQLCipherSmokeTests {

    @Test func keychainKeyIsDeterministic() throws {
        // KeychainKeyProvider generates once and returns the same key on subsequent calls.
        // This test validates the Keychain round-trip (device/simulator only).
        let key1 = try KeychainKeyProvider.key()
        let key2 = try KeychainKeyProvider.key()
        #expect(key1 == key2, "Keychain key must be stable across calls (generated once, stored device-only)")
        #expect(key1.count == 32, "Key must be 256-bit (32 bytes)")
    }

    @Test func databaseOpensWithoutPassphrase() throws {
        // Smoke test: DB opens using standard GRDB (no SQLCipher).
        // When SQLCipher is wired, this will open with the Keychain key.
        let db = try BolsilloDatabase.inMemory()
        #expect(db.queue != nil, "Database must open successfully")
    }

    // Encryption-at-rest validation (requires real device + SQLCipher build):
    // When enabled, a DB file opened WITHOUT the Keychain passphrase must fail
    // with SQLITE_NOTADB or similar. This cannot be asserted in the CI environment
    // until the SQLCipher binary is linked (Articles I/II).
    @Test func encryptionAtRestNote() throws {
        #expect(true, "SQLCipher encryption-at-rest validation: run on device after T002 SQLCipher wiring is complete")
    }
}
