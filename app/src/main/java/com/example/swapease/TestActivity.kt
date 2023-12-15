package com.example.swapease

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.swapease.data.models.Chat
import com.example.swapease.data.models.Message
import com.example.swapease.data.models.Product
import com.example.swapease.data.models.User
import com.example.swapease.databinding.ActivityTestBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val products = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.registerButton.setOnClickListener {
            registerUser("kullanic23232i@example.com", "password123")
        }

        // Ürün ekleme işlemi (örnek için bir buton tetiklemesi olarak kabul edelim)
        binding.addProductButton.setOnClickListener {
            addProduct("Ürün Adı", "Ürün Açıklaması")
        }

        // Ürün listesini alıp kullanıcıya gösterme


        // Kullanıcının bir ürüne tıklaması durumunda (örnek için bir ürün listesi öğesine tıklama olarak kabul edelim)
        binding.SHOWALL.setOnClickListener {

            loadProducts { produclist, exception ->
                if (exception == null) {
                    // Veriler başarıyla alındı, productList kullanılabilir
                    produclist?.get(0)?.publisherUid?.let { sendMessage(it, "Merhaba, ürün hakkında bilgi alabilir miyim?") }
                } else {
                    // Hata durumu
                    println("Ürünleri yüklerken hata oluştu: $exception")
                }
            }
            //val selectedProduct = list[list.size]

        }

    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    addUserToFirestore(user?.uid, "Kullanıcı Adı", email)
                } else {
                    println("Kullanıcı oluşturulurken hata oluştu: ${task.exception?.message}")
                }
            }
    }

    private fun addUserToFirestore(uid: String?, username: String, email: String) {
        if (uid != null) {
            val newUser = User(uid, username, email)
            db.collection("users").document(uid)
                .set(newUser)
                .addOnSuccessListener {
                    println("Kullanıcı Firestore'a eklendi.")
                }
                .addOnFailureListener { e ->
                    println("Kullanıcı eklenirken hata oluştu: $e")
                }
        }
    }

    private fun addProduct(productName: String, description: String) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val newProduct = Product(productName, description, uid)
            db.collection("products").document()
                .set(newProduct)
                .addOnSuccessListener {
                    println("Ürün Firestore'a eklendi.")
                }
                .addOnFailureListener { e ->
                    println("Ürün eklenirken hata oluştu: $e")
                }
        }
    }

    fun loadProducts(callback: (List<Product>?, Exception?) -> Unit) {
        db.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                val productList = mutableListOf<Product>()
                for (document in documents) {
                    val product = document.toObject(Product::class.java)
                    productList.add(product)
                }
                callback(productList, null)
            }
            .addOnFailureListener { e ->
                callback(null, e)
            }
    }

    private fun sendMessage(receiverUid: String, messageText: String) {
        val senderUid = auth.currentUser?.uid
        if (senderUid != null) {
            val newMessage = Message(senderUid, receiverUid, messageText, System.currentTimeMillis())
            val chatDocRef = db.collection("chats").document()
            chatDocRef.set(Chat(chatDocRef.id, listOf(newMessage)))
                .addOnSuccessListener {
                    println("Mesaj gönderildi.")
                }
                .addOnFailureListener { e ->
                    println("Mesaj gönderilirken hata oluştu: $e")
                }
        }
    }

}