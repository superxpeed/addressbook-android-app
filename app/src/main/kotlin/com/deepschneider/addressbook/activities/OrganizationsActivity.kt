package com.deepschneider.addressbook.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ListAdapter
import android.widget.ListView
import androidx.core.view.GravityCompat
import androidx.preference.PreferenceManager
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.adapters.OrganizationsListAdapter
import com.deepschneider.addressbook.dto.FilterDto
import com.deepschneider.addressbook.dto.OrganizationDto
import com.deepschneider.addressbook.dto.PageDataDto
import com.deepschneider.addressbook.dto.TableDataDto
import com.deepschneider.addressbook.listeners.OnSwipeTouchListener
import com.deepschneider.addressbook.utils.Constants
import com.deepschneider.addressbook.utils.Utils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class OrganizationsActivity : AbstractListActivity<OrganizationDto>() {

    private lateinit var searchEditTextLastUpdated: EditText
    private lateinit var searchEditTextLastComparator: EditText
    private lateinit var organizationsListView: ListView
    private lateinit var searchEditTextType: EditText
    private val lastUpdatedCalendar: Calendar = Calendar.getInstance()
    private var currentFilter: List<FilterDto>? = null
    private var start: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization)
        prepareListView()
        prepareActionBar(R.id.organizations_activity_drawer_layout)
        prepareFloatingActionButton()
        prepareSearchEditTextLastUpdated()
        prepareSearchEditTextType()
        prepareOrganizationSearchButton()
    }

    private fun prepareListView(){
        organizationsListView = findViewById(R.id.organizations_activity_list_view)
        organizationsListView.setOnItemClickListener { parent, _, position, _ ->
            val intent = Intent(applicationContext, PersonsActivity::class.java)
            val organizationDto: OrganizationDto = parent.adapter.getItem(position) as OrganizationDto
            intent.putExtra("organization", organizationDto)
            startActivity(intent)
        }
        organizationsListView.setOnTouchListener(object :
            OnSwipeTouchListener(this@OrganizationsActivity) {
            override fun onSwipeTop() {
                this@OrganizationsActivity.totalListSize?.let {
                    if (start * Constants.PAGE_SIZE < it) {
                        start++
                        updateList(getFilter())
                    }
                }
            }

            override fun onSwipeBottom() {
                if (start > 1) {
                    start--
                    updateList(getFilter())
                }
            }
        })
    }

    private fun prepareOrganizationSearchButton() {
        val organizationSearchButton =
            findViewById<Button>(R.id.organizations_activity_search_button)
        organizationSearchButton.setOnClickListener {
            mainDrawer.closeDrawer(GravityCompat.START)
            val filters = arrayListOf<FilterDto>()
            Utils.getTextFilterDto(
                this.getString(R.string.search_org_obj_id),
                findViewById<EditText>(R.id.organizations_activity_search_edit_text_id).text.toString()
            )?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                this.getString(R.string.search_org_obj_name),
                findViewById<EditText>(R.id.organizations_activity_search_edit_text_name).text.toString()
            )?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                this.getString(R.string.search_org_obj_address),
                findViewById<EditText>(R.id.organizations_activity_search_edit_text_address).text.toString()
            )?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                this.getString(R.string.search_org_obj_zip),
                findViewById<EditText>(R.id.organizations_activity_search_edit_text_zip).text.toString()
            )?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                this.getString(R.string.search_org_obj_type),
                findViewById<EditText>(R.id.organizations_activity_search_edit_text_type).text.toString()
            )?.let { it1 -> filters.add(it1) }
            val comparatorEnglish =
                findViewById<EditText>(R.id.organizations_activity_search_edit_text_date_comparator).text.toString()
            if (comparatorEnglish.isNotBlank() && comparatorEnglish != this.getString(R.string.no_value_placeholder)) {
                val actualComparatorIndex = this.resources.getStringArray(R.array.date_comparators_english).indexOf(comparatorEnglish)
                val actualComparator = this.resources.getStringArray(R.array.date_comparators)[actualComparatorIndex]
                Utils.getDateFilterDto(
                    this.getString(R.string.search_org_obj_last_updated),
                    findViewById<EditText>(R.id.organizations_activity_search_edit_text_date_last_updated).text.toString(),
                    actualComparator
                )?.let { it1 -> filters.add(it1) }
            }
            currentFilter = filters
            start = 1
            updateList(filters)
        }
    }

    private fun prepareSearchEditTextLastUpdated() {
        searchEditTextLastUpdated = findViewById(R.id.organizations_activity_search_edit_text_date_last_updated)
        searchEditTextLastUpdated.setOnClickListener {
            var isDataSet = false
            val dataPickerDialog = DatePickerDialog(
                this@OrganizationsActivity,
                { _, year, month, day ->
                    with(lastUpdatedCalendar) {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, day)
                    }
                    updateLabel()
                    isDataSet = true
                },
                lastUpdatedCalendar[Calendar.YEAR],
                lastUpdatedCalendar[Calendar.MONTH],
                lastUpdatedCalendar[Calendar.DAY_OF_MONTH]
            )
            dataPickerDialog.setOnDismissListener {
                if (!isDataSet) {
                    searchEditTextLastUpdated.text = null
                    searchEditTextLastUpdated.gravity = Gravity.LEFT
                }
            }
            dataPickerDialog.show()
        }
        searchEditTextLastComparator = findViewById(R.id.organizations_activity_search_edit_text_date_comparator)
        searchEditTextLastComparator.setOnClickListener {
            val builder = AlertDialog.Builder(this@OrganizationsActivity)
            builder.setTitle(R.string.choose_date_comparator).setItems(
                R.array.date_comparators_english
            ) { dialog, which ->
                if (which == 0) {
                    searchEditTextLastComparator.text = null
                    searchEditTextLastComparator.gravity = Gravity.LEFT
                } else {
                    searchEditTextLastComparator.setText(resources.getStringArray(R.array.date_comparators_english)[which])
                    searchEditTextLastComparator.gravity = Gravity.CENTER
                }
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun prepareSearchEditTextType() {
        searchEditTextType = findViewById(R.id.organizations_activity_search_edit_text_type)
        searchEditTextType.setOnClickListener {
            val builder = AlertDialog.Builder(this@OrganizationsActivity)
            builder.setTitle(R.string.choose_organization_type).setItems(
                R.array.org_types
            ) { dialog, which ->
                if (which == 0) searchEditTextType.text = null
                else searchEditTextType.setText(resources.getStringArray(R.array.org_types)[which])
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun prepareFloatingActionButton() {
        findViewById<FloatingActionButton>(R.id.organizations_activity_fab).setOnClickListener {
            startActivity(Intent(applicationContext, CreateOrEditOrganizationActivity::class.java))
        }
    }

    private fun updateLabel() {
        searchEditTextLastUpdated.setText(
            SimpleDateFormat(
                "MM/dd/yy", Locale.US
            ).format(lastUpdatedCalendar.time)
        )
        searchEditTextLastUpdated.gravity = Gravity.CENTER
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_organization, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) return true
        return when (item.itemId) {
            R.id.action_logout_organizations -> {
                logout()
                return true
            }
            R.id.action_sort_settings_organizations -> {
                showSortSettingsDialogs()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        loadSortSettings()
        updateList(getFilter())
    }

    private fun loadSortSettings() {
        sortName = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(Constants.SETTINGS_ORGANIZATION_LIST_SORT_FIELD, this.getString(R.string.search_org_obj_id))
            .toString()
        sortOrder = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(Constants.SETTINGS_ORGANIZATION_LIST_SORT_ORDER, "desc").toString()
    }

    override fun saveSortSettings() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putString(Constants.SETTINGS_ORGANIZATION_LIST_SORT_FIELD, sortName).commit()
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putString(Constants.SETTINGS_ORGANIZATION_LIST_SORT_ORDER, sortOrder).commit()
    }

    override fun getParentCoordinatorLayoutForSnackBar(): Int = R.id.organizations_activity_coordinator_layout

    override fun getRequestTag(): String = "ORGANIZATIONS_TAG"

    override fun getEmptyListView(): Int = R.id.organizations_activity_empty_list

    override fun getMainList(): ListView = organizationsListView

    override fun getProgressBar(): Int = R.id.organizations_activity_progress_bar

    override fun getTotalListSizeTextView(): Int = R.id.organizations_activity_list_total_size

    override fun getStartPage(): Int = start

    override fun getTargetCache(): String = Constants.ORGANIZATIONS_CACHE_NAME

    override fun getMainListType(): Type = object : TypeToken<PageDataDto<TableDataDto<OrganizationDto>>>() {}.type

    override fun getListAdapter(list: List<OrganizationDto>): ListAdapter = OrganizationsListAdapter(list, this@OrganizationsActivity)

    override fun getFilter(): List<FilterDto> = currentFilter ?: emptyList()

    override fun getFieldListObjNames(): Int = R.array.organizations_list_field_obj_names

    override fun getFieldListDisplayNames(): Int = R.array.organizations_list_field_display_names
}