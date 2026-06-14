import Foundation
import GRDB

/// Opens (or creates) the encrypted SQLite database. Depends on T020-T022.
/// SQLCipher passphrase: when GRDBCustomSQLite + SQLCipher are wired in (T002 upgrade),
/// uncomment the prepareDatabase block — the Keychain key is already generated.
public final class BolsilloDatabase: Sendable {
    public let queue: DatabaseQueue

    public init(path: String) throws {
        var config = Configuration()
        // SQLCipher hook — uncomment when GRDB is built with SQLCipher:
        // let passphraseKey = try KeychainKeyProvider.key()
        // config.prepareDatabase { db in
        //     try db.usePassphrase(passphraseKey)
        // }

        queue = try DatabaseQueue(path: path, configuration: config)
        try Self.migrate(queue)
    }

    public static func inMemory() throws -> BolsilloDatabase {
        let db = try BolsilloDatabase(path: ":memory:")
        return db
    }

    private static func migrate(_ queue: DatabaseQueue) throws {
        var migrator = DatabaseMigrator()
        DatabaseMigratorV1.register(in: &migrator)
        try migrator.migrate(queue)
    }
}
