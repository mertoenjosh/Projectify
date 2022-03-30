package com.mertoenjosh.projectify.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.mertoenjosh.projectify.activities.ProfilePageActivity

object Constants {

    const val USERS = "users"
    const val BOARDS = "boards"

    const val IMAGE = "image"

    const val NAME = "name"
    const val MOBILE = "mobile"
    const val ASSIGNED_TO = "assignedTo"
    const val DOCUMENT_ID = "documentId"
    const val TASK_LIST = "taskList"
    const val BOARD_DETAIL = "boardDetail"
    const val ID = "id"
    const val EMAIL: String = "email"
    const val BOARD_MEMBERS_LIST = "board_members_list"
    const val SELECT = "Select"
    const val UN_SELECT = "UnSelect"

    const val TASK_LIST_ITEM_POSITION = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION = "card_list_item_position"

    const val READ_STORAGE_REQUEST_CODE = 27
    const val PICK_IMAGE_REQUEST_CODE = 35

    fun showImageChooser(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        activity.startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(uri: Uri, activity: Activity): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri))
    }

}