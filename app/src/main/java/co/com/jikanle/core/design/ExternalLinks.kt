package co.com.jikanle.core.design

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import co.com.jikanle.R

fun openWebLink(context: Context, url: String): Boolean {
    val uri = Uri.parse(url)
    if (uri.scheme != "https" || uri.host.isNullOrBlank()) {
        Toast.makeText(context, R.string.link_unavailable, Toast.LENGTH_LONG).show()
        return false
    }
    return try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        true
    } catch (_: android.content.ActivityNotFoundException) {
        Toast.makeText(context, R.string.link_unavailable, Toast.LENGTH_LONG).show()
        false
    }
}
