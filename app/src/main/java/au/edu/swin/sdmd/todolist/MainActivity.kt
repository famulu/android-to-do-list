package au.edu.swin.sdmd.todolist

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.snackbar.Snackbar
import java.time.ZonedDateTime


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = "Channel 1"
        val description = "A description of the channel"
        val channel =
            NotificationChannel(CHANNEL_ID, name, NotificationManagerCompat.IMPORTANCE_HIGH)
        channel.description = description
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private fun createPendingIntent(context: Context, toDo: ToDo): PendingIntent {
            val intent = Intent(context.applicationContext, MyNotification::class.java).putExtra(
                ID_EXTRA, toDo.id.toInt()
            ).putExtra(TITLE_EXTRA, toDo.title)
            val pendingIntent = PendingIntent.getBroadcast(
                context.applicationContext,
                toDo.id.toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            return pendingIntent
        }

        fun scheduleNotification(toDo: ToDo, context: Context, view: View) {
            if (toDo.reminderDateTime.isAfter(ZonedDateTime.now())) {
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