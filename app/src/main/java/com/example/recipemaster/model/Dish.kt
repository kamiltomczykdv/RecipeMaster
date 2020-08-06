package com.example.recipemaster.model

data class Dish(
    val title: String,
    val description: String,
    val ingredients: ArrayList<String>,
    val preparing: ArrayList<String>,
    val imgs: ArrayList<String>
)