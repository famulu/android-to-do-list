package au.edu.swin.sdmd.todolist

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder

const val CHANNEL_ID = "CHANNEL_1"
const val TITLE_EXTRA = "TITLE_EXTRA"
const val ID_EXTRA = "ID_EXTRA"

class MyNotification : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val toDoId = intent.getIntExtra(ID_EXTRA, -1)

        val args = bundleOf("toDoId" to toDoId.toLong())

        val pendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.toDoDetailFragment)
            .setArguments(args)
            .createPendingIntent()

        val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(intent.getStringExtra(TITLE_EXTRA))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(toDoId, notification)
    }
}