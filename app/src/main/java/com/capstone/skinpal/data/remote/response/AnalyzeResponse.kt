package com.capstone.skinpal.data.remote.response

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class AnalyzeResponse(

    @field:SerializedName("public_url")
    val publicUrl: String,

    @field:SerializedName("notes")
    val notes: List<String>,

    @field:SerializedName("progress")
    val progress: Progress,

    @field:SerializedName("result_your_skinhealth")
    val resultYourSkinhealth: ResultYourSkinhealth,

    @field:SerializedName("recommendations")
    val recommendations: Recommendations
)

data class Progress(

    @field:SerializedName("percentage")
    val percentage: JsonElement?,

    @field:SerializedName("message")
    val message: String?
)

data class SkinConditions(

    @field:SerializedName("wrinkles")
    val wrinkles: JsonElement,

    @field:SerializedName("normal")
    val normal: JsonElement,

    @field:SerializedName("acne")
    val acne: JsonElement,

    @field:SerializedName("redness")
    val redness: JsonElement
)

data class MoisturizerItem(

    @field:SerializedName("image_url")
    val imageUrl: String,

    @field:SerializedName("product_id")
    val productId: String,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("description")
    val description: String,

    @field:SerializedName("ingredients")
    val ingredients: String,

    @field:SerializedName("type")
    val type: String
)

data class TonerItem(

    @field:SerializedName("image_url")
    val imageUrl: String,

    @field:SerializedName("product_id")
    val productId: String,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("description")
    val description: String,

    @field:SerializedName("ingredients")
    val ingredients: String,

    @field:SerializedName("type")
    val type: String
)

data class SerumItem(

    @field:SerializedName("image_url")
    val imageUrl: String,

    @field:SerializedName("product_id")
    val productId: String,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("description")
    val description: String,

    @field:SerializedName("ingredients")
    val ingredients: String,

    @field:SerializedName("type")
    val type: String
)

data class TreatmentItem(

    @field:SerializedName("image_url")
    val imageUrl: String,

    @field:SerializedName("product_id")
    val productId: String,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("description")
    val description: String,

    @field:SerializedName("ingredients")
    val ingredients: String,

    @field:SerializedName("type")
    val type: String
)

data class Recommendations(

    @field:SerializedName("moisturizer")
    val moisturizer: List<MoisturizerItem>,

    @field:SerializedName("treatment")
    val treatment: List<TreatmentItem>,

    @field:SerializedName("sunscreen")
    val sunscreen: List<SunscreenItem>,

    @field:SerializedName("toner")
    val toner: List<TonerItem>,

    @field:SerializedName("serum")
    val serum: List<SerumItem>,

    @field:SerializedName("facial_wash")
    val facialWash: List<FacialWashItem>
)

data class FacialWashItem(

    @field:SerializedName("image_url")
    val imageUrl: String,

    @field:SerializedName("product_id")
    val productId: String,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("description")
    val description: String,

    @field:SerializedName("ingredients")
    val ingredients: String,

    @field:SerializedName("type")
    val type: String
)

data class ResultYourSkinhealth(

    @field:SerializedName("skin_conditions")
    val skinConditions: SkinConditions,

    @field:SerializedName("skin_type")
    val skinType: String
)

data class SunscreenItem(

    @field:SerializedName("image_url")
    val imageUrl: String,

    @field:SerializedName("product_id")
    val productId: String,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("description")
    val description: String,

    @field:SerializedName("ingredients")
    val ingredients: String,

    @field:SerializedName("type")
    val type: String
)
