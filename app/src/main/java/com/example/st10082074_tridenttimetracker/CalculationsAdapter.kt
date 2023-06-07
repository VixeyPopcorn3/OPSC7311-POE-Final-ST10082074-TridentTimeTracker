package com.example.st10082074_tridenttimetracker
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class CalculationsAdapter(private val context: Context) : BaseAdapter() {
    private val projects = mutableListOf<Calculations>()

    fun setProjects(projectList: List<Calculations>) {
        projects.clear()
        projects.addAll(projectList)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = projects.size

    override fun getItem(position: Int): Calculations = projects[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_calculations, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val project = getItem(position)

        viewHolder.textProjectName.text = project.name
        viewHolder.textTotalHours.text = project.totalHours.toString()

        return view
    }

    private class ViewHolder(view: View) {
        val textProjectName: TextView = view.findViewById(R.id.textProjectName)
        val textTotalHours: TextView = view.findViewById(R.id.textTotalHours)
    }
}
