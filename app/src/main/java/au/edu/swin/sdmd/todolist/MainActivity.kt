package au.edu.swin.sdmd.todolist

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.findNavController
import au.edu.swin.sdmd.todolist.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.time.ZonedDateTime


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createNotificationChannel()

        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setCheckedItem(R.id.toDoListFragment)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.navView.setNavigationItemSelectedListener {
            findNavController(R.id.nav_host_fragment).navigate(it.itemId)
            binding.drawerLayout.close()
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
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
            return PendingIntent.getBroadcast(
                context.applicationContext,
                toDo.id.toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
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