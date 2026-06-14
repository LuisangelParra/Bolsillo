import Foundation
import Security

enum KeychainKeyProvider {
    private static let service = "com.bolsillo.db.key"
    private static let account = "grdb-passphrase"

    /// Returns the 256-bit DB key, generating and storing it on first call.
    static func key() throws -> Data {
        if let existing = try load() { return existing }
        return try generateAndStore()
    }

    private static func load() throws -> Data? {
        let query: [CFString: Any] = [
            kSecClass: kSecClassGenericPassword,
            kSecAttrService: service,
            kSecAttrAccount: account,
            kSecReturnData: true,
            kSecAttrAccessible: kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
        ]
        var result: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        if status == errSecItemNotFound { return nil }
        guard status == errSecSuccess else { throw KeychainError.loadFailed(status) }
        return result as? Data
    }

    private static func generateAndStore() throws -> Data {
        var bytes = [UInt8](repeating: 0, count: 32)
        let status = SecRandomCopyBytes(kSecRandomDefault, 32, &bytes)
        guard status == errSecSuccess else { throw KeychainError.generateFailed(status) }
        let key = Data(bytes)
        try store(key)
        return key
    }

    private static func store(_ key: Data) throws {
        let query: [CFString: Any] = [
            kSecClass: kSecClassGenericPassword,
            kSecAttrService: service,
            kSecAttrAccount: account,
            kSecValueData: key,
            kSecAttrAccessible: kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
        ]
        let status = SecItemAdd(query as CFDictionary, nil)
        guard status == errSecSuccess else { throw KeychainError.storeFailed(status) }
    }
}

enum KeychainError: Error {
    case loadFailed(OSStatus)
    case generateFailed(OSStatus)
    case storeFailed(OSStatus)
}
