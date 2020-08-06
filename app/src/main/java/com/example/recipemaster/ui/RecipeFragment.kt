package com.example.recipemaster.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.NetworkInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.example.recipemaster.R
import com.example.recipemaster.model.Dish
import com.example.recipemaster.model.User
import com.example.recipemaster.viewmodel.RecipeViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream

class RecipeFragment : Fragment() {
    private val path: String = "/storage/emulated/0/pictures"
    private val question: String = "Do you want to save the picture?"
    private val yesAnswer: String = "Yes"
    private val noAnswer: String = "No"
    private lateinit var title: TextView
    private lateinit var description: TextView
    private lateinit var ingredients: TextView
    private lateinit var preparing: TextView
    private lateinit var imagesRecycler: RecyclerView
    private lateinit var banner: TextView
    private lateinit var builder: AlertDialog.Builder
    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var ingredientsTitle: TextView
    private lateinit var preparingTitle: TextView
    private lateinit var imagesTitle: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private var recipeDisposable: Disposable? = null
    private var connectionDisposable: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.recipe_fragment, container, false)
        val user: User? = requireArguments().getParcelable(MainActivity.argumentName)
        init(view)
        setBar(user)
        checkConnection()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recipeDisposable?.dispose()
        connectionDisposable?.dispose()
        (activity as AppCompatActivity).supportActionBar?.title = MainActivity.titleMain
    }

    private fun checkConnection() {
        connectionDisposable = recipeViewModel.checkConnection(activity as Context)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { connectivity ->
                run {
                    if (connectivity.state() == NetworkInfo.State.CONNECTED) {
                        progressBar.visibility = View.VISIBLE
                        ingredientsTitle.visibility = View.VISIBLE
                        preparingTitle.visibility = View.VISIBLE
                        imagesTitle.visibility = View.VISIBLE
                        getDish()
                    } else {
                        Toast.makeText(
                            activity as Context,
                            "There is no internet connection",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        progressBar.visibility = View.INVISIBLE
                    }
                }
            }
    }

    private fun init(view: View) {
        title = view.findViewById(R.id.title_tv)
        description = view.findViewById(R.id.description_tv)
        ingredients = view.findViewById(R.id.ingredients_tv)
        preparing = view.findViewById(R.id.preparing_tv)
        preparingTitle = view.findViewById(R.id.preparing_title_tv)
        ingredientsTitle = view.findViewById(R.id.ingredients_title_tv)
        imagesTitle = view.findViewById(R.id.images_tv)
        banner = view.findViewById(R.id.banner)
        recipeViewModel = ViewModelProviders.of(activity!!).get(RecipeViewModel::class.java)
        recyclerView = view.findViewById(R.id.images_rv)
        progressBar = view.findViewById(R.id.progressBar)
        (activity as AppCompatActivity).supportActionBar?.title = MainActivity.titleRecipe
    }

    private fun initList(urls: ArrayList<String>) {
        imageAdapter = ImageAdapter((activity as Context), urls) { image -> onImageClick(image) }
        recyclerView.adapter = imageAdapter
        progressBar.visibility = View.INVISIBLE
        imageAdapter.notifyDataSetChanged()
    }

    private fun onImageClick(image: ImageView) {
        popUp(image)
    }

    private fun getDish() {
        recipeDisposable = recipeViewModel.getDish()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError {
                Toast.makeText(activity as Context, it.toString(), Toast.LENGTH_LONG).show()
            }
            .subscribe { dish ->
                run {
                    setUi(dish)
                    initList(dish.imgs)
                }
            }
    }

    private fun setUi(dish: Dish) {
        title.text = dish.title
        description.text = dish.description
        ingredients.text = dish.ingredients.joinToString(separator = "") { it -> "- $it\n" }
        preparing.text =
            dish.preparing.joinToString(separator = "") { it -> (dish.preparing.indexOf(it) + 1).toString() + ". " + it + "\n" + "\n" }
    }

    @SuppressLint("SetTextI18n")
    private fun setBar(user: User?) {
        if (user != null) {
            banner.visibility = View.VISIBLE
            banner.text = "Logged as ${user.firstName} ${user.lastName}"
        }
    }

    private fun popUp(image: ImageView) {
        builder = AlertDialog.Builder(activity as Context)
        builder.setMessage(question)
            .setCancelable(false)
            .setPositiveButton(yesAnswer) { dialog, _ ->
                run {
                    if ((activity as Context).checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        (activity as Context).checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    ) {
                        savePicture(image)
                    } else {
                        Toast.makeText(
                            (activity as Context),
                            "Please, check for permissions",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    dialog.cancel()
                }
            }
            .setNegativeButton(noAnswer) { dialog, _ -> dialog.cancel() }
            .create()
            .show()
    }

    private fun savePicture(image: ImageView) {
        val drawable: BitmapDrawable = image.drawable as BitmapDrawable
        val bitMap = drawable.bitmap
        val dir: File = File(path)
        dir.mkdir()
        val file: File = File(dir, System.currentTimeMillis().toString() + ".jpg")
        val outputStream = FileOutputStream(file)
        bitMap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        Toast.makeText(activity as Context, "Image has been successfully saved", Toast.LENGTH_LONG)
            .show()
        outputStream.flush()
        outputStream.close()
    }
}