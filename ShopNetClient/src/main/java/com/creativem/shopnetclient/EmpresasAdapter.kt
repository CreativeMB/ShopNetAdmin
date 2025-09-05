package com.creativem.shopnetclient


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.creativem.shopnetclient.databinding.ItemEmpresaBinding
class EmpresasAdapter(
    private val empresas: List<Empresa>,
    private val onClick: (Empresa) -> Unit // callback
) : RecyclerView.Adapter<EmpresasAdapter.EmpresaViewHolder>() {

    inner class EmpresaViewHolder(val binding: ItemEmpresaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpresaViewHolder {
        val binding = ItemEmpresaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EmpresaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmpresaViewHolder, position: Int) {
        val empresa = empresas[position]
        with(holder.binding) {
            tvNombreEmpresa.text = empresa.nombre
            tvDescripcionEmpresa.text = empresa.descripcion
            tvDireccionEmpresa.text = "📍 ${empresa.direccion}"
            tvHorarioEmpresa.text = "⏰ ${empresa.horarioAtencion}"
            tvTelefonoEmpresa.text = "📞 ${empresa.whatsapp}"

            Glide.with(ivLogoEmpresa.context)
                .load(empresa.logoUrl)
                .placeholder(R.drawable.icono)
                .into(ivLogoEmpresa)

            Glide.with(ivBannerEmpresa.context)
                .load(empresa.portadaUrl)
                .placeholder(R.drawable.icono)
                .into(ivBannerEmpresa)

            // 👉 Click en toda la tarjeta
            root.setOnClickListener {
                onClick(empresa)
            }
        }
    }

    override fun getItemCount(): Int = empresas.size
}
