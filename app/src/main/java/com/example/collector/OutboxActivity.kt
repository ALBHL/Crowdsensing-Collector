package com.example.collector

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log.d
import android.view.Menu
import android.view.MenuItem
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_inbox.*
//import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.activity_outbox.*
import kotlinx.android.synthetic.main.images_row.view.*
import kotlinx.android.synthetic.main.outbox_file_row.*
import kotlinx.android.synthetic.main.outbox_file_row.view.*
import java.sql.Array
import kotlin.reflect.typeOf

class OutboxActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outbox)

        supportActionBar?.title = "Outbox"

        //TODO - inferencer
        button_inferpic.setOnClickListener{
            val intent = Intent(this, InferencerActivity::class.java)
            startActivity(intent)
        }

        val context = this
        val db = DataBaseHandler(context)
        val data = db.readData()

        fetchUsers(data)
    }

    companion object {
        val USER_KEY = "USER_KEY"
        val USER_ID = "USER_ID"
    }


    private fun fetchUsers(data: MutableList<User>) {
        val adapter = GroupAdapter<ViewHolder>()
        for (i in 0 until data.size) {
            if (data[i].cur_stage == "validated") {
                adapter.add(UserItem(data[i]))
            }
        }
        adapter.setOnItemClickListener { item, view ->
            val userItem = item as UserItem
            val intent = Intent(view.context, ConfirmSendOutActivity::class.java)
            intent.putExtra(USER_ID, userItem.user.id.toString())
            startActivity(intent)
        }
        recycleview_outbox.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, InboxActivity::class.java)
                startActivity(intent)
            }
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
        menuInflater.inflate(R.menu.nav_menu_outbox, menu)
        return super.onCreateOptionsMenu(menu)
    }
}

class UserItem(val user: User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        // will be called in the list of user object
        viewHolder.itemView.outbox_query_title.text = user.name
//        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageview_new_message)
        val imageURL = user.profileurl
        if (imageURL != "") {
            Picasso.get().load(imageURL).into(viewHolder.itemView.imageViewprofile)
        }
    }

    override fun getLayout(): Int {
        return R.layout.outbox_file_row
    }
}

// this is super tedious
//class CustomAdapter: RecyclerView.Adapter<ViewHolder> {
//  override fun onBindViewHolder(p0:, p1: Int) {
//    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//  }
//}
