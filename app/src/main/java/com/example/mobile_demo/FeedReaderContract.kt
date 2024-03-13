package com.example.mobile_demo

import android.provider.BaseColumns

class FeedReaderContract {
    object FeedEntry : BaseColumns {
        const val TABLE_NAME = "entry"
        const val ID = "id"
        const val COLUMN_NAME_TITLE = "title"
        const val COLUMN_NAME_SUBTITLE = "subtitle"
    }
}

