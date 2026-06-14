import Foundation
import GRDB
import BolsilloDomain

public enum CategorySeed {
    // Derived from shared-assets/taxonomy/category-taxonomy.json (Article VI)
    public static let all: [BolsilloDomain.Category] = [
        BolsilloDomain.Category(id: "food",                  nameKey: "category.food",                   icon: "fork.knife",                    colorToken: "restaurantes"),
        BolsilloDomain.Category(id: "food.groceries",        nameKey: "category.food.groceries",         icon: "cart",                          colorToken: "mercado"),
        BolsilloDomain.Category(id: "food.restaurants",      nameKey: "category.food.restaurants",       icon: "takeoutbag.and.cup.and.straw",  colorToken: "restaurantes"),
        BolsilloDomain.Category(id: "food.coffee",           nameKey: "category.food.coffee",            icon: "cup.and.saucer",                colorToken: "cafe"),
        BolsilloDomain.Category(id: "transport",             nameKey: "category.transport",              icon: "car",                           colorToken: "transporte"),
        BolsilloDomain.Category(id: "transport.fuel",        nameKey: "category.transport.fuel",         icon: "fuelpump",                      colorToken: "transporte"),
        BolsilloDomain.Category(id: "transport.taxi",        nameKey: "category.transport.taxi",         icon: "car.side",                      colorToken: "transporte"),
        BolsilloDomain.Category(id: "transport.transit",     nameKey: "category.transport.transit",      icon: "bus",                           colorToken: "transporte"),
        BolsilloDomain.Category(id: "housing",               nameKey: "category.housing",                icon: "house",                         colorToken: "vivienda"),
        BolsilloDomain.Category(id: "housing.rent",          nameKey: "category.housing.rent",           icon: "key",                           colorToken: "vivienda"),
        BolsilloDomain.Category(id: "utilities",             nameKey: "category.utilities",              icon: "bolt",                          colorToken: "servicios"),
        BolsilloDomain.Category(id: "utilities.electricity", nameKey: "category.utilities.electricity",  icon: "bolt.fill",                     colorToken: "servicios"),
        BolsilloDomain.Category(id: "utilities.internet",    nameKey: "category.utilities.internet",     icon: "wifi",                          colorToken: "servicios"),
        BolsilloDomain.Category(id: "health",                nameKey: "category.health",                 icon: "cross.case",                    colorToken: "salud"),
        BolsilloDomain.Category(id: "leisure",               nameKey: "category.leisure",                icon: "gamecontroller",                colorToken: "ocio"),
        BolsilloDomain.Category(id: "education",             nameKey: "category.education",              icon: "book",                          colorToken: "educacion"),
        BolsilloDomain.Category(id: "shopping",              nameKey: "category.shopping",               icon: "bag",                           colorToken: "ropa"),
        BolsilloDomain.Category(id: "income",                nameKey: "category.income",                 icon: "arrow.down.circle",             colorToken: "ingreso"),
        BolsilloDomain.Category(id: "transfer",              nameKey: "category.transfer",               icon: "arrow.left.arrow.right",        colorToken: "otros"),
        BolsilloDomain.Category(id: "other",                 nameKey: "category.other",                  icon: "ellipsis.circle",               colorToken: "otros"),
    ]

    public static func seed(_ db: BolsilloDatabase) throws {
        try db.queue.write { dbq in
            guard try CategoryRecord.fetchCount(dbq) == 0 else { return }
            for cat in all {
                try CategoryRecord.from(cat).insert(dbq)
            }
        }
    }
}
