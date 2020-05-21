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
import archiveasia.jp.co.hakenman.extension.month
import archiveasia.jp.co.hakenman.extension.week
import archiveasia.jp.co.hakenman.extension.year
import archiveasia.jp.co.hakenman.model.DetailWork
import archiveasia.jp.co.hakenman.model.Worksheet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.current_worksheet_list_item.view.*
import java.util.Date

class WorksheetListAdapter(
    private var list: MutableList<Worksheet> = mutableListOf(),
    private var listener: WorksheetListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_CURRENT_WORKSHEET = 1
        private const val TYPE_PAST_WORKSHEET = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_CURRENT_WORKSHEET -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.current_worksheet_list_item, parent, false)
                CurrentWorksheetViewHolder(view)
            }
            TYPE_PAST_WORKSHEET -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.past_worksheet_list_item, parent, false)
                PastWorksheetViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_CURRENT_WORKSHEET else TYPE_PAST_WORKSHEET
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        when (holder) {
            is CurrentWorksheetViewHolder -> holder.bindView(item)
            is PastWorksheetViewHolder -> holder.bindView(item)
        }
    }

    fun replaceWorksheetList(list: List<Worksheet>) {
        val diffResult = DiffUtil.calculateDiff(DiffCallback(this.list, list))
        this.list.clear()
        this.list.addAll(list)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class CurrentWorksheetViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bindView(worksheet: Worksheet) {
            val detailWorkList = worksheet.detailWorkList
            var index = detailWorkList.size - 1
            val chartItemList = ArrayList<DetailWork>()
            while (index != -1) {
                val detailWork = detailWorkList[index]
                if (detailWork.workFlag && detailWork.duration != null && detailWork.duration != 0.0) {
                    chartItemList.add(0, detailWork)
                }
                if (chartItemList.size == 7) {
                    break
                }
                index -= 1
            }
            setChart(chartItemList)

            itemView.setOnClickListener {
                listener.onClickItem(adapterPosition, worksheet)
            }
            itemView.setOnLongClickListener {
                listener.onLongClickItem(adapterPosition)
                true
            }

            itemView.year_textView.text = worksheet.workDate.year()
            itemView.month_textView.text = worksheet.workDate.month()
            val now = Date()
            itemView.day_textView.text = now.day()
            itemView.week_textView.apply {
                text = now.week()
                val color = when(now.dayOfWeek()) {
                    1 -> Color.RED
                    7 -> Color.BLUE
                    else -> Color.BLACK
                }
                setTextColor(color)
            }
            itemView.workHour_textView.text = worksheet.workTimeSum.toString()
            itemView.workDay_textView.text = worksheet.workDaySum.toString()
        }

        private fun setChart(chartItemList: ArrayList<DetailWork>) {
            itemView.line_chart.apply {
                visibility = View.VISIBLE
                val entries = ArrayList<Entry>()
                chartItemList.forEach {
                    entries.add(Entry(entries.size * 1f, it.duration?.toFloat() ?: 0f))
                }

                // チャートデータライン設定
                val lineDataSet = LineDataSet(entries, context.getString(R.string.work_time_column))
                lineDataSet.lineWidth = 2f
                lineDataSet.color = Color.BLACK
                lineDataSet.circleRadius = 4.5f
                lineDataSet.setCircleColor(Color.BLACK)
                lineDataSet.setDrawCircleHole(false)
                lineDataSet.valueTextSize = 8f

                val data = LineData(lineDataSet)
                this.data = data
                if (data.entryCount == 0) {
                    // 「データなし」メッセージ表示のため
                    clear()
                }

                // チャート縦設定
                xAxis.axisMinimum = -0.5f
                xAxis.axisMaximum = 6.5f
                xAxis.setDrawLabels(false)
                xAxis.setDrawGridLines(false)
                xAxis.position = XAxis.XAxisPosition.BOTH_SIDED

                // チャート左側設定
                axisLeft.axisLineColor = Color.GRAY
                axisLeft.labelCount = 4
                axisLeft.setDrawLabels(false)
                axisLeft.axisMaximum = data.yMax + 1f
                axisLeft.axisMinimum = data.yMin - 1f

                // チャート右側設定
                axisRight.axisLineColor = Color.GRAY
                axisRight.setDrawGridLines(false)
                axisRight.setDrawLabels(false)

                description.text = context.getString(R.string.chart_description)
                setNoDataText(context.getString(R.string.chart_no_data))
                setNoDataTextColor(Color.BLACK)

                setTouchEnabled(false)
                setPinchZoom(false)

                // チャートの空白調節
                // TODO: Magic Numberどうにかしたい
                setViewPortOffsets(1f, viewPortHandler.offsetTop() / 2, 1f, viewPortHandler.offsetBottom())

                invalidate()
            }
        }
    }

    inner class PastWorksheetViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bindView(worksheet: Worksheet) {
            itemView.setOnClickListener {
                listener.onClickItem(adapterPosition, worksheet)
            }
            itemView.setOnLongClickListener {
                listener.onLongClickItem(adapterPosition)
                true
            }

            itemView.year_textView.text = worksheet.workDate.year()
            itemView.month_textView.text = worksheet.workDate.month()
            itemView.workHour_textView.text = worksheet.workTimeSum.toString()
            itemView.workDay_textView.text = worksheet.workDaySum.toString()
        }
    }

    interface WorksheetListener {
        fun onClickItem(index: Int, worksheet: Worksheet)

        fun onLongClickItem(index: Int)
    }
}