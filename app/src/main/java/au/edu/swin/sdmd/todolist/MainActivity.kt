package au.edu.swin.sdmd.todolist

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationManagerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import java.time.ZonedDateTime


class MainActivity : AppCompatActivity() {
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()

        val drawerLayout = findViewById<DrawerLayout>(R.id.my_drawer_layout)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
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