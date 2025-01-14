package com.example.permissionapp.ui.fragment


import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.permissionapp.R
import com.example.permissionapp.data.remote.model.RegisterModel
import com.example.permissionapp.databinding.FragmentRegisterBinding
import com.example.permissionapp.ui.viewModel.RegisterViewModel
import com.example.permissionapp.utils.Constants
import com.example.permissionapp.utils.Dynamics
import com.example.permissionapp.utils.MessangeResponse
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var navController: NavController
    private lateinit var register: RegisterModel

    private val registerViewModel: RegisterViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        openPicker()
        spinner()
        setAdmin()
        validFields()
        setObservers()
        setCc()
    }

    private fun setCc() {
        binding.edtNumberId.text=Dynamics.CC
    }

    private fun setObservers() {
        registerViewModel.isRegister.observe(viewLifecycleOwner) { isRegister ->
            if (isRegister) {
                view?.let { launchLoginFragment(it) }
                Toast.makeText(context, "Usuario Registrado con exito", Toast.LENGTH_SHORT).show()
            }
        }
        registerViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.linearLayoutLoading.isVisible = isLoading
            } else {
                binding.linearLayoutLoading.isVisible = false
            }
        }
        registerViewModel.error.observe(viewLifecycleOwner) {
            try {
                view?.let {  Snackbar.make(it,"Usuario ya registrado", Snackbar.LENGTH_SHORT).show()}
            }catch (e: Exception){
                Toast.makeText(
                    context,
                    "Error: $e",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
        registerViewModel.connectivity.observe(viewLifecycleOwner) { connectivity ->
            Toast.makeText(context, "" + connectivity, Toast.LENGTH_SHORT).show()
        }
    }

    private fun spinner() {
        val typeId=resources.getStringArray(R.array.type_id)
        if (binding.spinTypeId !=null){
            val adapter= ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,typeId)
            binding.spinTypeId.adapter=adapter
        }
    }

    private fun openPicker() {
        binding.datePicker.setOnClickListener{
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val datePicker=DatePickerFragment({day,month,year -> onDateSelect(day,month, year) })
        datePicker.show(parentFragmentManager,"datePicker")
    }
    fun onDateSelect(day:Int, month: Int, year: Int){
        if (day<10 && month<10){
            binding.datePicker.setText("$year-0$month-0$day")
        }

        else if (month<10){
            binding.datePicker.setText("$year-0$month-$day")
        }
        else if(day<10){
            binding.datePicker.setText("$year-$month-0$day")
        }
        else
            binding.datePicker.setText("$year-$month-$day")
    }
    private fun launchLoginFragment(view: View) {
        navController = Navigation.findNavController(view)
        navController.navigate(R.id.loginFragment)
    }
    private fun setAdmin(){
        binding.btnAdmin.setOnClickListener{
            AdminDialogFragment(
                onSubmitClickListener = { password->
                    if (password.equals(Constants.PASSWORD_ADMIN)){
                        binding.showAdmin.visibility=View.VISIBLE
                        binding.layoutBtnAdmin.visibility=View.GONE
                    }
                    else{
                        view?.let {  Snackbar.make(it,"Clave admin incorrecta", Snackbar.LENGTH_SHORT).show()}
                    }
                }
            ).show(parentFragmentManager,"Dialog")
        }

    }
    private fun validFields(){
        binding.btnNext.setOnClickListener{
            val name: String=binding.edtName.text.toString()
            val lastName:String=binding.edtLastname.text.toString()
            val email:String=binding.edtEmail.text.toString()
            val password:String=binding.edtPassword.text.toString()
            val passwordValidation: String=binding.edtPassword2.text.toString()
            val typeId:String=binding.spinTypeId.selectedItem.toString()
            val identification: String = binding.edtNumberId.text.toString()
            val birthday: String=binding.datePicker.text.toString()
            val dataPolity: CheckBox=binding.checkPolity
            val dataCondition: CheckBox= binding.checkCondition
            val adminROle: CheckBox =binding.checkAdmin

            if (name.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && passwordValidation.isNotEmpty() && typeId.isNotEmpty() && identification.isNotEmpty() && birthday.isNotEmpty()){
                if(Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    if (password.length >= 6 && password.equals(passwordValidation)){
                        if (dataPolity.isChecked && dataCondition.isChecked){
                            userRegister(name,lastName,email,password,typeId,identification,birthday,adminROle)
                        }else{
                            view?.let {  Snackbar.make(it,"Por favor acepte condiciones", Snackbar.LENGTH_SHORT).show()}
                        }
                    }else{
                        view?.let {  Snackbar.make(it,"Minimo 6 caracteres para la contraseña o confirmacion invalidada", Snackbar.LENGTH_SHORT).show()}
                    }
                }else{
                    view?.let {  Snackbar.make(it,"Correo invalido", Snackbar.LENGTH_SHORT).show()}
                }
            }else{
                view?.let {  Snackbar.make(it,"Ningun campo puede estar vacio", Snackbar.LENGTH_SHORT).show()}
            }
        }
    }
    private fun userRegister(
        name: String,
        lastName: String,
        email: String,
        password: String,
        typeId: String,
        identification: String,
        birthday: String,
        adminRole: CheckBox
    ){
        register= RegisterModel(name,lastName,email,password,typeId,identification,birthday,setUpRol(adminRole))
        lifecycleScope.launch{
            registerViewModel.onRegisterUser(register)
        }
    }

    private fun setUpRol(adminRole: CheckBox):ArrayList<String>{
        val rol: ArrayList<String> = ArrayList()
        if (adminRole.isChecked)
            rol.add(Constants.ADMIN)
        else{
            rol.add(Constants.USER)
        }
        return rol

    }


}