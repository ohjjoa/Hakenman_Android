package archiveasia.jp.co.hakenman.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import archiveasia.jp.co.hakenman.R
import archiveasia.jp.co.hakenman.extension.day
import archiveasia.jp.co.hakenman.extension.dayOfWeek
import archiveasia.jp.co.hakenman.extension.hourMinute
import archiveasia.jp.co.hakenman.extension.week
import archiveasia.jp.co.hakenman.extension.yearMonth
import archiveasia.jp.co.hakenman.model.DetailWork
import kotlinx.android.synthetic.main.daliy_work_item.view.*

class DailyWorkAdapter(
    private val detailWorkList: MutableList<DetailWork>,
    private val listener: DailyWorkListener
) : RecyclerView.Adapter<DailyWorkAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.daliy_work_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = detailWorkList.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(detailWorkList[position])
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun replaceDailyWorkList(list: List<DetailWork>) {
        val diffResult = DiffUtil.calculateDiff(DiffCallback(detailWorkList, list))
        detailWorkList.clear()
        detailWorkList.addAll(list)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        fun bindView(detailWork: DetailWork) {
            itemView.setOnClickListener {
                listener.onClick(adapterPosition)
            }
            with (detailWork) {
                itemView.day_textView.text = workDate.day()
                itemView.week_textView.apply {
                    text = workDate.week()
                    val textColor = when (workDate.dayOfWeek()) {
                        1 -> Color.RED
                        7 -> Color.BLUE
                        else -> Color.BLACK
                    }
                    setTextColor(textColor)
                }
                itemView.workFlag_textView.text =  if (workFlag) "O" else "X"
                beginTime?.let {
                    itemView.startWork_textView.text = it.hourMinute()
                }
                endTime?.let {
                    itemView.endWork_textView.text = it.hourMinute()
                }

                breakTime?.let {
                    itemView.breakTime_textView.text = it.hourMinute()
                }
                duration?.let {
                    itemView.workTime_textView.text = it.toString()
                }
                itemView.note_textView.text = note
            }
        }
    }

    class DiffCallback(
        private val oldData: List<DetailWork>,
        private val newData: List<DetailWork>
    ) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldData[oldItemPosition].workDate.yearMonth() == newData[newItemPosition].workDate.yearMonth()
        }

        override fun getOldListSize(): Int = oldData.size

        override fun getNewListSize(): Int = newData.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldData[oldItemPosition] == newData[newItemPosition]
        }
    }

    interface DailyWorkListener {
        fun onClick(position: Int)
    }
}
