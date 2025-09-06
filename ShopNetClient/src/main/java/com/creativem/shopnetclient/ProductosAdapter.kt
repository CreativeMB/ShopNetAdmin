package com.creativem.shopnetclient

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.creativem.shopnetclient.databinding.ItemProductoBinding

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

        // Cargar imagen del producto
        Glide.with(holder.itemView.context)
            .load(producto.imagenUrl)
            .placeholder(R.drawable.icono)
            .error(R.drawable.icono)
            .into(b.ivProducto)

        // Nombre, referencia y descripción
        b.tvNombre.text = producto.nombre
        b.tvReferencia.text = producto.referencia
        b.tvDescripcion.text = producto.descripcion

        // Estado de agotado
        if (producto.agotado) {
            b.tvAgotado.visibility = View.VISIBLE
            b.tvPrecio.visibility = View.GONE
            b.tvPrecioPromocionSobreImagen.visibility = View.GONE
        } else {
            b.tvAgotado.visibility = View.GONE
            b.tvPrecio.visibility = View.VISIBLE

            if (producto.promocion && producto.valorPromocion != null) {
                // Precio normal tachado
                b.tvPrecio.apply {
                    text = "$${producto.valor}"
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                // Precio promocional sobre la imagen, abajo derecha
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
