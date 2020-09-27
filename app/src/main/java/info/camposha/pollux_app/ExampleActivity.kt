package info.camposha.pollux_app

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import info.camposha.pollux.PolluxAdapter
import info.camposha.pollux_app.databinding.CardImageOverlayBottomTextBinding
import kotlinx.android.synthetic.main.activity_example.*
import java.util.*
import kotlin.collections.ArrayList

open class ExampleActivity : AppCompatActivity() {

    private var adapter: PolluxAdapter<Item>? = null
    var pageToFetch = 1
    var data: ArrayList<String> = ArrayList()
    private var reachedEnd = false

    private fun show(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun generateRandomWords(numberOfWords: Int): String? {
        val randomStrings = arrayOfNulls<String>(numberOfWords)
        val random = Random()
        val sb = StringBuilder()
        for (i in 0 until numberOfWords) {
            val word =
                CharArray(random.nextInt(8) + 3) // words of length 3 through 10. (1 and 2 letter words are boring.)
            for (j in word.indices) {
                word[j] = ('a'.toInt() + random.nextInt(26)).toChar()
            }
            randomStrings[i] = String(word)
            val cap =
                randomStrings[i]!!.substring(0, 1).toUpperCase() + randomStrings[i]!!
                    .substring(1)
            sb.append("$cap ")
        }
        return sb.toString()
    }
    /**
     * Let's simulate a download operation
     */
    fun simulateDownload(data: List<Item>): MutableLiveData<List<Item>> {
        val timer = Timer()
        val mLiveData = MutableLiveData<List<Item>>()
        timer.schedule(object : TimerTask() {
            override fun run() {
                mLiveData.postValue(data)
            }
        }, 3000)
        return mLiveData
    }

    private fun createItems(): List<Item> {
        val end: Int = rv.layoutManager!!.itemCount + 10

        val start: Int = if (pageToFetch <= 1) {
            1
        } else {
            rv.layoutManager!!.itemCount + 1
        }
        return (start..end).map {
            Item(
                generateRandomWords(1),
                generateRandomWords(2), generateRandomWords(10),
                "https://picsum.photos/1000/700?image=$it"
            )
        }
    }
    fun loadImg(url: String?, imageView: ImageView) {
        if(url.isNullOrEmpty()){
            return
        }
        val requestOptions = RequestOptions.placeholderOf(R.drawable.load_glass).error(R.drawable.gallery_roll)
            .dontTransform()
            .onlyRetrieveFromCache(false)

        Glide.with(imageView).load(url).apply(requestOptions).into(imageView)
    }
    /**
     * Setup the adapter. Pass a layoutmanager to recyclerview before setting up Infinity
     */
    private fun setupAdapter() {
        pb.visibility = View.GONE
        rv.layoutManager = GridLayoutManager(this, 2)

        adapter =
            PolluxAdapter.with<Item, CardImageOverlayBottomTextBinding>(R.layout.card_image_overlay_bottom_text) { adapterPosition, model, binding ->
                loadImg(model.imageURL,binding.image)
                binding.titleTV.text = model.text1
                binding.image.setOnClickListener {
                    val items = arrayOf("One", "Two", "Three")
                    MaterialAlertDialogBuilder(
                        this,
                        R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog
                    )
                        .setTitle(model.text1)
                        .setItems(items) { _, which ->
                            // Respond to item chosen
                            show(model.text1+" and "+ items[which])
                        }
                        .show()
                }
            }
        adapter?.addAll(createItems())
        rv.adapter = adapter

        adapter?.setupLoadMorePagination(rv) {
            simulateDownload(createItems()).observe(this@ExampleActivity, Observer {
                adapter?.addAll(it)
                adapter?.notifyDataSetChanged()
                adapter?.markLoadMoreAsComplete()
            })
            pageToFetch++

            true
        }
    }

    override fun onResume() {
        super.onResume()
        simulateDownload(createItems()).observe(this@ExampleActivity, Observer {
            adapter?.addAll(it)
            pb.visibility = View.GONE
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)
        setupAdapter()
    }
}