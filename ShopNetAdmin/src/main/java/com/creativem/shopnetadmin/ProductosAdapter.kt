package com.creativem.shopnetadmin

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.creativem.shopnetadmin.databinding.ItemProductoBinding

class ProductosAdapter(
    private val listaProductos: List<Producto>
) : RecyclerView.Adapter<ProductosAdapter.ProductoViewHolder>() {

    inner class ProductoViewHolder(val binding: ItemProductoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = listaProductos[position]
        val b = holder.binding

        // Imagen
        Glide.with(holder.itemView.context)
            .load(producto.imagenUrl)
            .placeholder(R.drawable.icono) // tu placeholder
            .error(R.drawable.icono)
            .into(b.ivProducto)

        // Nombre y referencia
        b.tvNombre.text = producto.nombre
        b.tvReferencia.text = producto.referencia
        b.tvDescripcion.text = producto.descripcion

        // Agotado
        if (producto.agotado) {
            b.tvAgotado.visibility = android.view.View.VISIBLE
            b.tvPrecio.text = ""
            b.tvPrecioPromocion.text = ""
        } else {
            b.tvAgotado.visibility = android.view.View.GONE

            // Precios
            if (producto.promocion && producto.valorPromocion != null) {
                b.tvPrecio.apply {
                    text = "$${producto.valor}"
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                b.tvPrecioPromocion.text = "$${producto.valorPromocion}"
            } else {
                b.tvPrecio.apply {
                    text = "$${producto.valor}"
                    paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
                b.tvPrecioPromocion.text = ""
            }
        }
    }

    override fun getItemCount(): Int = listaProductos.size
}
