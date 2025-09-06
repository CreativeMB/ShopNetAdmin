package com.creativem.shopnetadmin

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.creativem.shopnetadmin.databinding.ItemProductoBinding

class ProductosAdapter(
    private val listaProductos: List<Producto>,
    private val onProductoClick: (Producto) -> Unit,
    private val onProductoLongClick: (Producto) -> Unit  // 🔹 callback para click largo
) : RecyclerView.Adapter<ProductosAdapter.ProductoViewHolder>() {

    inner class ProductoViewHolder(val binding: ItemProductoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            // Click normal
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onProductoClick(listaProductos[position])
                }
            }

            // Click largo
            binding.root.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onProductoLongClick(listaProductos[position])
                    true
                } else false
            }
        }
    }

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
            .placeholder(R.drawable.icono)
            .error(R.drawable.icono)
            .into(b.ivProducto)

        // Nombre, referencia y descripción
        b.tvNombre.text = producto.nombre
        b.tvReferencia.text = producto.referencia
        b.tvDescripcion.text = producto.descripcion

        // Agotado
        if (producto.agotado) {
            b.tvAgotado.visibility = View.VISIBLE
            b.tvPrecio.visibility = View.GONE
            b.tvPrecioPromocionSobreImagen.visibility = View.GONE
        } else {
            b.tvAgotado.visibility = View.GONE
            b.tvPrecio.visibility = View.VISIBLE

            if (producto.promocion && producto.valorPromocion != null) {
                // Precio normal tachado abajo
                b.tvPrecio.apply {
                    text = "$${producto.valor}"
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                // Precio promoción sobre la imagen, abajo derecha
                b.tvPrecioPromocionSobreImagen.apply {
                    text = "$${producto.valorPromocion}"
                    visibility = View.VISIBLE
                }
            } else {
                // Sin promoción
                b.tvPrecio.apply {
                    text = "$${producto.valor}"
                    paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
                b.tvPrecioPromocionSobreImagen.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = listaProductos.size
}
