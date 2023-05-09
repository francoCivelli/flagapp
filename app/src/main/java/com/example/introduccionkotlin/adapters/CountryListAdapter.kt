package com.example.introduccionkotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.introduccionkotlin.R
import com.example.introduccionkotlin.databinding.ItemCountryBinding
import com.example.introduccionkotlin.model.Country
import com.example.introduccionkotlin.util.getProgressDrawable
import com.example.introduccionkotlin.util.loadImage

class CountryListAdapter(var countries: ArrayList<Country>, private val isHome: Boolean,
                         private val listener: OnCountryListener) : RecyclerView.Adapter<CountryListAdapter.CountryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CountryViewHolder (
        ItemCountryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = countries.size

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bind(countries[position])
    }

    inner class CountryViewHolder (private val binding: ItemCountryBinding) : RecyclerView.ViewHolder(binding.root){
        private val progressDrawable = getProgressDrawable(binding.root.context)
        fun bind(country: Country){
            with(binding){
                name.text = country.countryName
                capital.text = country.capital
                imageView.loadImage(country.flag, progressDrawable)
                data.setOnClickListener{
                    listener.countrySelected(country)
                }
                if(isHome){
                    action.setImageDrawable(ResourcesCompat.getDrawable(binding.root.resources, R.drawable.ic_add_country, root.context.theme))
                    action.setOnClickListener{
                        listener.addCountry(country)
                    }
                } else {
                    action.setImageDrawable(ResourcesCompat.getDrawable(binding.root.resources, R.drawable.ic_erase_country, root.context.theme))
                    action.setOnClickListener{
                        listener.removeCountry(country)
                    }
                }
            }
        }
    }

    fun updateCountries (newCountries: List<Country>){
        countries.clear()
        countries.addAll(newCountries)
        notifyDataSetChanged()
    }

    interface OnCountryListener {
        fun countrySelected(country: Country)
        fun addCountry(country: Country)
        fun removeCountry(country: Country)
    }

}