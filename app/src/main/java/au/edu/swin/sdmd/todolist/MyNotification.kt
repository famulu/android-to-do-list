package au.edu.swin.sdmd.todolist

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.google.android.material.snackbar.Snackbar
import java.time.ZonedDateTime

const val CHANNEL_ID = "CHANNEL_1"
const val TITLE_EXTRA = "TITLE_EXTRA"
const val ID_EXTRA = "ID_EXTRA"

class MyNotification : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val toDoId = intent.getIntExtra(ID_EXTRA, -1)

        val args = bundleOf("toDoId" to toDoId.toLong())

        val pendingIntent = NavDeepLinkBuilder(context).setGraph(R.navigation.nav_graph)
            .setDestination(R.id.toDoDetailFragment).setArguments(args).createPendingIntent()

        val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(intent.getStringExtra(TITLE_EXTRA))
            .setPriority(NotificationCompat.PRIORITY_MAX).setContentIntent(pendingIntent)
            .setAutoCancel(true).build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(toDoId, notification)
    }

    companion object {
        private fun createPendingIntent(context: Context, toDo: ToDo): PendingIntent {
            val intent = Intent(context.applicationContext, MyNotification::class.java).putExtra(
                ID_EXTRA, toDo.id.toInt()
            ).putExtra(TITLE_EXTRA, toDo.title)
            return PendingIntent.getBroadcast(
                context.applicationContext,
                toDo.id.toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        fun createNotificationChannel(context: Context) {
            val name = "Channel 1"
            val description = "A description of the channel"
            val channel =
                NotificationChannel(CHANNEL_ID, name, NotificationManagerCompat.IMPORTANCE_HIGH)
            channel.description = description
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        fun scheduleNotification(toDo: ToDo, context: Context, view: View) {
            if (toDo.reminderDateTime?.isAfter(ZonedDateTime.now()) == true) {
                val pendingIntent = createPendingIntent(context, toDo)

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val time = toDo.reminderDateTime.toInstant().toEpochMilli()
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)

                Snackbar.make(
                    view,
                    "Notification has been scheduled for ${toDo.reminderDateTime}",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        fun cancelNotification(toDo: ToDo, context: Context) {
            val pendingIntent = createPendingIntent(context, toDo)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }

}