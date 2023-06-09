package com.deepschneider.addressbook.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.databinding.ActivityCreateOrEditContactBinding
import com.deepschneider.addressbook.dto.ContactDto
import com.deepschneider.addressbook.utils.Utils
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CreateOrEditContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateOrEditContactBinding
    private var contactDto: ContactDto? = null
    private lateinit var contactTypes: Array<String>
    private val fieldValidation = BooleanArray(3)

    inner class TextFieldValidation(private val view: View) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            when (view.id) {
                R.id.data -> validateDataEditText()
                R.id.type -> validateTypeEditText()
                R.id.desc -> validateDescEditText()
            }
            updateSaveButtonState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if(!resources.configuration.isNightModeActive)
            setTheme(R.style.Theme_Addressbook_Light)
        super.onCreate(savedInstanceState)
        binding = ActivityCreateOrEditContactBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        prepareExtras()
        prepareTypeEditText()
        prepareLayout()
        prepareDeleteContactButton()
        prepareAddOrApplyButton()
        updateUi(contactDto)
        setupListeners()
        validateDataEditText()
        validateDescEditText()
        validateTypeEditText()
        updateSaveButtonState()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                currentFocus?.clearFocus() ?: run {
                    finish()
                }
            }
        })
    }

    private fun prepareLayout() {
        contactTypes = this.resources.getStringArray(R.array.contact_types)
    }

    private fun prepareExtras() {
        val extra = Utils.getSerializable(this, "contact", ContactDto::class.java)
        if (extra != null) contactDto = extra
    }

    private fun prepareTypeEditText() {
        binding.type.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this@CreateOrEditContactActivity)
            builder.setTitle(R.string.choose_contact_type).setItems(
                R.array.contact_types
            ) { dialog, which ->
                if (which == 0) binding.type.text = null
                else binding.type.setText(resources.getStringArray(R.array.contact_types)[which])
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun prepareDeleteContactButton() {
        binding.deleteContactButton.setOnClickListener {
            MaterialAlertDialogBuilder(this@CreateOrEditContactActivity)
                .setTitle(this.getString(R.string.delete_contact_confirmation))
                .setPositiveButton(R.string.contact_deletion_delete) { _, _ ->
                    val data = Intent()
                    data.putExtra("contact", contactDto)
                    data.putExtra("delete", true)
                    setResult(RESULT_OK, data)
                    finish()
                }
                .setNegativeButton(R.string.contact_deletion_cancel, null).show()
        }
    }

    private fun prepareAddOrApplyButton() {
        binding.addApplyButton.setOnClickListener {
            val targetContactDto = if (contactDto == null) ContactDto() else contactDto
            targetContactDto?.data = binding.data.text.toString()
            targetContactDto?.description = binding.desc.text.toString()
            targetContactDto?.type = (this.resources.getStringArray(R.array.contact_types)
                .indexOf(binding.type.text.toString()) - 1).toString()
            val data = Intent()
            data.putExtra("contact", targetContactDto)
            setResult(RESULT_OK, data)
            finish()
        }
    }

    private fun validateDataEditText() {
        val value = binding.data.text.toString().trim()
        if (value.isEmpty()) {
            binding.dataLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[1] = false
        } else if (value.length > 500) {
            binding.dataLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[1] = false
        } else {
            binding.dataLayout.error = null
            fieldValidation[1] = true
        }
    }

    private fun validateDescEditText() {
        val value = binding.desc.text.toString().trim()
        if (value.isEmpty()) {
            binding.descLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[0] = false
        } else if (value.length > 100) {
            binding.descLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[0] = false
        } else {
            binding.descLayout.error = null
            fieldValidation[0] = true
        }
    }

    private fun validateTypeEditText() {
        if (binding.type.text.toString().trim().isEmpty()) {
            binding.typeLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[2] = false
        } else {
            binding.typeLayout.error = null
            fieldValidation[2] = true
        }
    }

    private fun updateSaveButtonState() {
        binding.addApplyButton.isEnabled = fieldValidation.all { it }
    }

    private fun setupListeners() {
        binding.type.addTextChangedListener(TextFieldValidation(binding.type))
        binding.data.addTextChangedListener(TextFieldValidation(binding.data))
        binding.desc.addTextChangedListener(TextFieldValidation(binding.desc))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateUi(contactDto: ContactDto?) {
        contactDto?.let { contact ->
            binding.id.setText(contact.id)
            contact.type?.let { binding.type.setText(contactTypes[it.toInt() + 1]) }
            binding.data.setText(contact.data)
            binding.desc.setText(contact.description)
            contact.type?.let { contactType ->
                title = " " + contactTypes[contactType.toInt() + 1]
                binding.deleteContactButton.visibility = View.VISIBLE
            }
            binding.addApplyButton.text = this.getString(R.string.action_apply_contact_changes)
        } ?: run {
            binding.addApplyButton.text = this.getString(R.string.action_add_contact)
            binding.deleteContactButton.visibility = View.GONE
        }
    }
}