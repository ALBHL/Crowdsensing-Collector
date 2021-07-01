package com.example.collector

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_inbox.*
import kotlinx.android.synthetic.main.activity_setdata.*

class InboxActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)

        supportActionBar?.title = "Inbox"

        //TODO - login
//        verifyUserIsLoggedIn()

        button_inbox.setOnClickListener {
            val intent = Intent(this, SetDataActivity::class.java)
            startActivity(intent)
        }

//        deleteBox.setOnClickListener{
//            val intent = Intent(this, DeleteActivity::class.java)
//            startActivity(intent)
//        }

        button_takepic.setOnClickListener {
            val intent = Intent(this, CollectorActivity::class.java)
            startActivity(intent)
        }

        button_refresh_inbox.setOnClickListener {
            recycleview_inbox.setVisibility(View.GONE)
            Log.d("NewMessage", "heyyyy")
            val ref = FirebaseDatabase.getInstance().getReference("/users")
            ref.get().addOnSuccessListener {
                Log.i("firebase", "Got value ${it.value}")
            }.addOnFailureListener {
                Log.e("firebase", "Error getting data", it)
            }
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                val db = DataBaseHandler(this@InboxActivity)
                override fun onDataChange(p0: DataSnapshot) {
                    db.updateData()
                    if (p0.exists()) {
                        val adapter = GroupAdapter<ViewHolder>()
                        var tasks = ArrayList<Task>()
                        p0.children.forEach {
                            val creatorModel = it.getValue(CreatorModel::class.java)
                            it.child("tasks").children.forEach {
                                it.getValue(Task::class.java)?.let { it1 -> tasks.add(it1) }
                            }
                            if (creatorModel != null && tasks != null) {
                                for (i in 0 until tasks.size) {
                                    if (tasks[i].cur_stage == "to collect") {
                                        var user = User(
                                            tasks[i].task_id,
                                            tasks[i].task_name,
                                            1,
                                            creatorModel.profileurl,
                                            tasks[i].task_description,
                                            tasks[i].range_radius,
                                            tasks[i].range_angle,
                                            tasks[i].latitude,
                                            tasks[i].longitude
                                        )
                                        adapter.add(UserItem(user))
                                        db.insertData(user)
                                    }
                                }
                            }
                        }
                        adapter.setOnItemClickListener { item, view ->
                            val userItem = item as UserItem
                            val intent = Intent(view.context, CollectorActivity::class.java)
                            intent.putExtra(USER_KEY, userItem.user.imageurl)
                            intent.putExtra(ROW_ID, userItem.user.task_id)
                            intent.putExtra(ROW_NAME, userItem.user.task_name)
                            val locationData = listOf(userItem.user.latitude, userItem.user.longitude, userItem.user.range_radius, userItem.user.range_angle)
                            intent.putExtra(USER_LOCATION, locationData.joinToString(separator = ";"))
                            startActivity(intent)
                        }
                        recycleview_inbox.setVisibility(View.VISIBLE)
                        recycleview_inbox.adapter = adapter
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }



    }

    companion object {
        val USER_KEY = "USER_KEY"
        val ROW_ID = "ROW_ID"
        val ROW_NAME = "ROW_NAME"
        val USER_LOCATION = "USER_LOCATION"
    }

    private fun fetchUsers(data: MutableList<User>) {
        val adapter = GroupAdapter<ViewHolder>()
        for (i in 0 until data.size) {
            if (data[i].cur_stage == "to collect") {
                adapter.add(UserItem(data[i]))
            }
        }
        adapter.setOnItemClickListener { item, view ->
            val userItem = item as UserItem
            val intent = Intent(view.context, CollectorActivity::class.java)
            intent.putExtra(USER_KEY, userItem.user.imageurl)
            intent.putExtra(ROW_ID, userItem.user.id.toString())
            intent.putExtra(ROW_NAME, userItem.user.task_name)
            startActivity(intent)
        }
        recycleview_inbox.adapter = adapter
    }

    //TODO - login
//    private fun verifyUserIsLoggedIn() {
//        val uid = FirebaseAuth.getInstance().uid
//        if (uid == null) {
//            val intent = Intent(this, LoginActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)    // clear off the back stack
//            startActivity(intent)
//        }
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, OutboxActivity::class.java)
                startActivity(intent)
            }
            // TODO - login
//            R.id.menu_sign_out -> {
//                FirebaseAuth.getInstance().signOut()
//                val intent = Intent(this, LoginActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
//            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

}
