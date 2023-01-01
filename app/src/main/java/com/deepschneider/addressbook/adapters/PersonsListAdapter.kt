package com.deepschneider.addressbook.adapters

import android.app.Activity
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.dto.PersonDto

class PersonsListAdapter(
    private val persons: List<PersonDto>,
    private val activity: Activity
) : ArrayAdapter<PersonDto>(activity, R.layout.persons_list, persons) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = activity.layoutInflater.inflate(R.layout.persons_list, null, true)
        rowView.findViewById<TextView>(R.id.person_name).text =
            persons[position].firstName + " " + persons[position].lastName
        rowView.findViewById<TextView>(R.id.person_resume).text =
            Html.fromHtml(persons[position].resume, Html.FROM_HTML_MODE_COMPACT)
        rowView.findViewById<TextView>(R.id.person_id).text = persons[position].id
        rowView.findViewById<TextView>(R.id.person_salary).text = persons[position].salary
        return rowView
    }
}