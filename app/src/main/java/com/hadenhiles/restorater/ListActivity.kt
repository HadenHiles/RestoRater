package com.hadenhiles.restorater

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.item_restaurant.view.*

class ListActivity : AppCompatActivity() {

    // connect to Firestore
    val db = FirebaseFirestore.getInstance()
    private var adapter: RestaurantAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        // set our recyclerview to use LinearLayout
        restaurantsRecyclerView.layoutManager = LinearLayoutManager(this)

        // query the db for all restaurants
        val query = db.collection("restaurants").orderBy("name", Query.Direction.ASCENDING)

        // pass query results to the Recycler adapter
        val options = FirestoreRecyclerOptions.Builder<Restaurant>().setQuery(query, Restaurant::class.java).build()
        adapter = RestaurantAdapter(options)
        restaurantsRecyclerView.adapter = adapter

        addFab.setOnClickListener {
            // navigate to Main Activity to add a Restaurant
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }

        logoutFab.setOnClickListener {
            // Sign out the user
            FirebaseAuth.getInstance().signOut();
            finish()

            // reload the SignIn
            val intent = Intent(applicationContext, SignInActivity::class.java)
            startActivity(intent)
        }
    }

    // tell adapter to start watching data for changes
    override fun onStart() {
        super.onStart()
        adapter!!.startListening()

        // check if the user is signed in (optional but good to know)
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            val intent = Intent(applicationContext, SignInActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStop() {
        super.onStop()
        if (adapter != null) {
            adapter!!.stopListening()
        }
    }

    // create inner classes needed to bind the data to the recyclerview
    private inner class RestaurantViewHolder internal constructor(private val view: View) : RecyclerView.ViewHolder(view) {}

    private inner class RestaurantAdapter internal constructor(options: FirestoreRecyclerOptions<Restaurant>) :
            FirestoreRecyclerAdapter<Restaurant, RestaurantViewHolder>(options) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
            // Inflate the item_restaurant.xml layout template to populate the recyclerview
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)

            return RestaurantViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: RestaurantViewHolder,
            position: Int,
            model: Restaurant
        ) {
            // populate restaurant name and rating into the matching textView and rating bar for each item in the list
            holder.itemView.nameTextView.text = model.name
            holder.itemView.ratingBar.rating = model.rating!!.toFloat() // convert to float so it matches the ratingBar type

            holder.itemView.setOnClickListener {
                val intent = Intent(applicationContext, RestaurantActivity::class.java)
                intent.putExtra("restaurantId", model.id)
                intent.putExtra("name", model.name)
                startActivity(intent)
            }
        }

    }
}