package com.example.swapease

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swapease.data.models.Product
import com.example.swapease.databinding.ActivityProfileBinding
import com.example.swapease.ui.adapters.ProductListAdapter
import com.example.swapease.ui.viewmodels.ProductViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    //private val db = FirebaseFirestore.getInstance()
    //private val auth = FirebaseAuth.getInstance()
    private lateinit var productViewModel: ProductViewModel
    private lateinit var productListAdapter: ProductListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Check condition
        /*if (firebaseUser != null) {
            // When firebase user is not equal to null set image on image view
            Glide.with(this@ProfileActivity).load(firebaseUser.photoUrl).into(binding.ivImage)
            // set name on text view
            binding.tvName.text = firebaseUser.displayName
        }*/

       /* // Initialize sign in client
        googleSignInClient = GoogleSignIn.getClient(this@ProfileActivity, GoogleSignInOptions.DEFAULT_SIGN_IN)
        binding.btLogout.setOnClickListener {
            // Sign out from google
            googleSignInClient.signOut().addOnCompleteListener { task ->
                // Check condition
                if (task.isSuccessful) {
                    // When task is successful sign out from firebase
                    firebaseAuth.signOut()
                    // Display Toast
                    Toast.makeText(applicationContext, "Logout successful", Toast.LENGTH_SHORT).show()
                    // Finish activity
                    finish()
                }
            }
        }*/

    }

    /*
    private fun addItemToDatabase(itemName: String, price: String, description: String) {
        // Kullanıcının UID'sini al
        val currentUserUid = auth.currentUser?.uid

        // Kullanıcının UID'si null değilse veritabanına öğeyi ekle
        if (currentUserUid != null) {
            val product = hashMapOf(
                "sellerUid" to currentUserUid,
                "productName" to itemName,
                "price" to price,
                "description" to description,
                //"imageUrl" to imageUrl
            )

            db.collection("products")
                .add(product)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "Item added with ID: ${documentReference.id}")
                    // Başarılı ekleme durumunda yapılacak işlemler
                    Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding item", e)
                    // Hata durumunda yapılacak işlemler
                    Toast.makeText(this, "Error adding item", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Kullanıcı oturum açmamışsa bir hata mesajı göster
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    // Kullanıcıya özgü öğeleri göstermek için bu metot çağrılabilir
    private fun getUserItems() {
        // Kullanıcının UID'sini al
        val currentUserUid = auth.currentUser?.uid

        // Kullanıcının UID'si null değilse veritabanından öğeleri al
        if (currentUserUid != null) {
            db.collection("products")
                .whereEqualTo("sellerUid", currentUserUid)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val items = mutableListOf<Product>()
                    for (document in querySnapshot) {
                        val product = document.toObject(Product::class.java)
                        items.add(product)
                    }
                    // items listesini kullanarak UI güncelleme işlemleri yapılabilir
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error getting items", e)
                    // Hata durumunda yapılacak işlemler
                }
        } else {
            // Kullanıcı oturum açmamışsa bir hata mesajı göster
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val TAG = "AddItemActivity"
    }

    */
}