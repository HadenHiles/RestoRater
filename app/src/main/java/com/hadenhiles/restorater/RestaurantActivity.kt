package com.hadenhiles.restorater

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_restaurant.*
import kotlinx.android.synthetic.main.item_comment.view.*

class RestaurantActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()
    private var adapter: RestaurantActivity.CommentAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant)

        val restaurantId = intent.getStringExtra("restaurantId")
        restaurantNameTextView.text = intent.getStringExtra("name")?.toString()

        // set our recyclerview to use LinearLayout
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)

        // query the db for all restaurants
        var query = db.collection("comments").whereEqualTo("restaurantId", restaurantId).orderBy("username", Query.Direction.ASCENDING)

        // pass query results to the Recycler adapter
        val options = FirestoreRecyclerOptions.Builder<Comment>().setQuery(query, Comment::class.java).build()
        adapter = CommentAdapter(options)
        commentsRecyclerView.adapter = adapter

        saveCommentButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val body = bodyEditText.text.toString()
            if(username.isNotEmpty() && body.isNotEmpty() && restaurantId!!.isNotEmpty()) {
                val id = db.collection("comments").document().id
                val comment = Comment(id, username, body, restaurantId)
                db.collection("comments").document(comment.commentId!!).set(comment)

                usernameEditText.setText("")
                bodyEditText.setText("")

                Toast.makeText(this, "Comment added successfully", Toast.LENGTH_LONG).show()

                // TODO: find a way to refresh the recycler view data without reloading the entire activity
                finish()
                startActivity(intent)
            } else {
                Toast.makeText(this, "please make sure all fields are filled", Toast.LENGTH_LONG).show()
            }
        }

        backFab.setOnClickListener {
            val intent = Intent(applicationContext, ListActivity::class.java)
            startActivity(intent)
        }
    }

    // tell adapter to start watching data for changes
    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        if (adapter != null) {
            adapter!!.stopListening()
        }
    }

    // create inner classes needed to bind the data to the recyclerview
    private inner class CommentViewHolder internal constructor(private val view: View) : RecyclerView.ViewHolder(view) {}

    private inner class CommentAdapter internal constructor(options: FirestoreRecyclerOptions<Comment>) :
        FirestoreRecyclerAdapter<Comment, CommentViewHolder>(options) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
            // Inflate the item_restaurant.xml layout template to populate the recyclerview
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)

            return CommentViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: CommentViewHolder,
            position: Int,
            model: Comment
        ) {
            // populate restaurant name and rating into the matching textView and rating bar for each item in the list
            holder.itemView.usernameTextView.text = model.username
            holder.itemView.bodyTextView.text = model.body
        }
    }
}