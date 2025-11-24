package com.example.kingoftokyo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MonsterAdapter(
    private val monsters: List<Monster>,
    private val onMonsterSelected: (Monster) -> Unit
) : RecyclerView.Adapter<MonsterAdapter.MonsterViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonsterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_monster, parent, false)
        return MonsterViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonsterViewHolder, position: Int) {
        val monster = monsters[position]
        holder.bind(monster)
        holder.itemView.isSelected = selectedPosition == position

        holder.itemView.setOnClickListener {
            if (selectedPosition != holder.adapterPosition) {
                notifyItemChanged(selectedPosition)
                selectedPosition = holder.adapterPosition
                notifyItemChanged(selectedPosition)
                onMonsterSelected(monster)
            }
        }
    }

    override fun getItemCount() = monsters.size

    class MonsterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.monsterName)
        private val statsTextView: TextView = itemView.findViewById(R.id.monsterStats)
        private val imageView: ImageView = itemView.findViewById(R.id.monsterImage)

        fun bind(monster: Monster) {
            nameTextView.text = monster.name
            statsTextView.text = "❤️ ${monster.healthPoints}   ⭐ ${monster.victoryPoints}"
            imageView.setImageResource(monster.image)
        }
    }
}
