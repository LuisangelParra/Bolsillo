package com.bolsillo.data.db

import androidx.room.migration.Migration

/**
 * Schema v1 baseline + a versioned migration harness (Article IX). The list is
 * empty in 001 — every future schema bump appends a [Migration] here, ships with
 * a migration test that seeds sample rows, and is rejected if it loses data.
 */
object BolsilloMigrations {
    val ALL: Array<Migration> = emptyArray()
}
